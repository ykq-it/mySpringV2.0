package com.my.spring.framework.webmvc.servlet;

import com.my.spring.framework.annotation.MyController;
import com.my.spring.framework.annotation.MyRequestMapping;
import com.my.spring.framework.context.MyApplicationContext;
import com.my.spring.framework.webmvc.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName MyDispatcherServlet
 * @Description TODO servlet生命周期有init，service，destory组成。  Servlet作为MVC的启动入口  委派模式
 * @Author ykq
 * @Date 2020/5/18
 * @Version v1.0.0
 */
public class MyDispatcherServlet extends HttpServlet {

    // TODO 干嘛用的？
    private final String LOCATION = "contextConfigLocation";

    // TODO 这里很经典？　HandlerMapping里有controller、method、url的patter
    private List<MyHandlerMapping> handlerMappings = new ArrayList<>();

    // TODO 干嘛用的？
    private Map<MyHandlerMapping, MyHandlerAdapter> handlerAdapters = new HashMap<>();

    // TODO 干嘛用的？
    private List<MyViewResolver> viewResolvers = new ArrayList<>();

    private MyApplicationContext context;

    /**
     * 功能描述： 完成IoC容器的初始化和SpringMVC9大组件的初始化
     * @author ykq
     * @date 2020/5/18 19:46
     * @param
     * @return
     */
    @Override
    public void init(ServletConfig config) {
        // TODO debug看一下参数是怎么传来的
        // 相当于初始化了IoC容器
        context = new MyApplicationContext(config.getInitParameter(LOCATION));
        // 初始化策略
        initStrategies(context);
    }


    private void initStrategies(MyApplicationContext context) {
        // 有九种策略
        // 针对每个用户请求，都会经过一些处理策略处理，最终才能有结果输出
        // 每种策略可以自定义干预，但是最终的结果都一致
        /******************这里就是九大组件******************/
        // 初始化文件上传解析器，如果请求类型是Mutipart，将通过MutipartResolver进行文件上传解析
        initMutipartResolver(context);

        // 初始化本地化解析器
        initLocaleResolver(context);

        // 初始化主题解析器
        initThemeResolver(context);

        // ******通过HandlerMapping将请求映射到处理器，HandlerMapping用来保存Controller中配置的RequestMapping和Method的对应关系
        initHandlerMappings(context);

        // ******通过HandlerAdapter进行多类型的参数动态匹配，HandlerAdapters用来动态匹配Method参数，包括类转换和动态赋值
        initHandlerAdapters(context);

        // 如果执行过程中遇到异常，将交给HandlerExceptionResolver来解析
        initHandlerExceptionResolvers(context);

        // 直接将请求解析到视图名
        initRequestToViewNameTranslator(context);

        // 通过ViewResolvers实现动态模板解析
        // 自己解析一套模板语言
        // 通过ViewResolvers将逻辑视图解析到具体视图实现
        initViewResolvers(context);

        // Flash映射管理器
        initFlashMapManager(context);
    }

    /**
     * 功能描述： 将Controller中配置的RequestMapping和Method一一对应
     * @author ykq
     * @date 2020/5/18 20:18
     * @param
     * @return
     */
    private void initHandlerMappings(MyApplicationContext context) {
        // 按照我们通常的理解应该是一个Map
        // Map<String, Method> map;
        // map.put(url, Method);

        // 首先从容器中获取所有的实例
        String[] beanNames = context.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            // 到了MVC层对外提供的方法只有一个getBean()方法。获取beanName对应的一个实例。
            Object controller = context.getBean(beanName);
            // 反射获取类型类
            Class<?> clazz = controller.getClass();

            if (!clazz.isAnnotationPresent(MyController.class)) {
                continue;
            }

            String baseUrl = "";

            // 获取Controller的RequestMapping的value
            if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            // 扫描所有public类型的方法
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                    continue;
                }

                MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
                String regex = ("/" + baseUrl + requestMapping.value().replaceAll("\\*", ".").replaceAll("/+", "/"));
                // TODO 看一下这里是干嘛的
                Pattern pattern = Pattern.compile(regex);

                this.handlerMappings.add(new MyHandlerMapping(controller, method, pattern));
                System.out.println("Mapping" + regex + "," + method);;
            }
        }
    }

    /**
     * 功能描述： 参数适配器
     * @author ykq
     * @date 2020/5/18 21:13
     * @param
     * @return
     */
    private void initHandlerAdapters(MyApplicationContext context) {
        // 在初始化阶段，我们能做的就是，将这些参数的名字或者类型按照一定得顺序保存下来。因为后面用反射调用的时候，传的形参是一个数组。可以通过记录这些参数的位置index，逐个从数组中取值，这样就和参数的顺序无关了
        for (MyHandlerMapping handlerMapping : this.handlerMappings) {
            // 每个方法都有一个参数列表，这里保存的是形参列表  TODO ？？？？？？？？？？？？
            this.handlerAdapters.put(handlerMapping, new MyHandlerAdapter());
        }
    }

    /**
     * 功能描述： 视图解析器
     * @author ykq
     * @date 2020/5/18 21:20
     * @param 
     * @return 
     */
    private void initViewResolvers(MyApplicationContext context) {
        // 在页面中输入http://localhost/first.html，解析页面名字和模板文件关联的问题
        // TODO 看一下配置在哪了，看一下是什么
        String templateRoot = context.getConfig().getProperty("templateRoot");
        // TODO 获取资源文件的路径？？
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        // TODO 获取文件夹
        File templateRootDir = new File(templateRootPath);

        for (File template : templateRootDir.listFiles()) {
            // TODO 为什么传templateRoot，应该是template吧？？？
            this.viewResolvers.add(new MyViewResolver(templateRoot));
        }
    }





    private void initFlashMapManager(MyApplicationContext context) {
    }

    private void initRequestToViewNameTranslator(MyApplicationContext context) {
    }

    private void initHandlerExceptionResolvers(MyApplicationContext context) {
    }

    private void initThemeResolver(MyApplicationContext context) {
    }

    private void initLocaleResolver(MyApplicationContext context) {
    }

    private void initMutipartResolver(MyApplicationContext context) {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatcher(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 功能描述：
     * @author ykq
     * @date 2020/5/19 20:56
     * @param
     * @return
     */
    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        // 根据用户请求的URL获取一个Handler
        MyHandlerMapping handler = getHandler(req);

        if (null == handler) {
            processDispatcherResult(req, resp, new MyModelAndView("404"));
            return;
        }

        MyHandlerAdapter handlerAdapter = getHandlerAdapter(handler);
    }

    /**
     * 功能描述：
     * @author ykq
     * @date 2020/5/19 20:56
     * @param
     * @return
     */
    private MyHandlerAdapter getHandlerAdapter(MyHandlerMapping handler) {
        if (this.handlerAdapters.isEmpty()) {
            return null;
        }
        MyHandlerAdapter handlerAdapter = this.handlerAdapters.get(handler);

        // 判断处理器适配器是否支持当前的处理器映射器
        if (handlerAdapter.supports(handler)) {
            return handlerAdapter;
        }
        return null;
    }

    /**
     * 功能描述： 没有相应的处理器，则生成404页面
     * @author ykq
     * @date 2020/5/19 19:39
     * @param
     * @return
     */
    private void processDispatcherResult(HttpServletRequest req, HttpServletResponse resp, MyModelAndView mv) throws Exception {
        // 调用viewResolver的resolverViewName()方法
        if (null == mv) {
            return;
        }

        if (this.viewResolvers.isEmpty()) {
            return;
        }

//        if (this.viewResolvers != null) {   不太好，自以为用size>0比较好
        if (0 < this.viewResolvers.size()) {
            for (MyViewResolver viewResolver : this.viewResolvers) {
                MyView view = viewResolver.resolveViewName(mv.getViewName(), null);
                if (null != view) {
                    view.render(mv.getModel(), req, resp);
                    return;
                }
            }
        }
    }


    // TODO 这段要好好看看
    private MyHandlerMapping getHandler(HttpServletRequest req) {
        if (this.handlerMappings.isEmpty()) {
            return null;
        }
        //getRequestURI:/test/test.jsp
        //getRequestURL:http://localhost:8080/test/test.jsp
        String url = req.getRequestURI();

        // 是jsp中获取路径的一种方式，返回当前页面所在的应用的名字。
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        for (MyHandlerMapping handler : this.handlerMappings) {
            Matcher matcher = handler.getPattern().matcher(url);
            // url是否匹配handler的正则规则
            if (!matcher.matches()) {
                continue;
            }

            return handler;
        }
        return null;
    }
}
