package com.my.spring.framework.webmvc.servlet;

import com.my.spring.framework.annotation.MyController;
import com.my.spring.framework.annotation.MyRequestMapping;
import com.my.spring.framework.context.MyApplicationContext;
import com.my.spring.framework.webmvc.*;
import lombok.extern.slf4j.Slf4j;

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
 * @Description TODO servlet生命周期有init，service，destroy组成。  Servlet作为MVC的启动入口  委派模式
 * @Author ykq
 * @Date 2020/5/18
 * @Version v1.0.0
 */
@Slf4j
public class MyDispatcherServlet extends HttpServlet {

    // init()时传入web.xml对应的config，做为标签<init-param>中的key
    private final String LOCATION = "contextConfigLocation";

    // TODO 这里很经典？　HandlerMapping里有controller、method、url的patter
    private List<MyHandlerMapping> handlerMappings = new ArrayList<>();

    // TODO 干嘛用的？Adapter和handler的关联关系，用于通过handler获取Adapter
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
        // 传入的config是web.xml里配置的标签servlet，
        // 传入config的属性parameters的值，返回context相当于初始化了IoC容器
        context = new MyApplicationContext(config.getInitParameter(LOCATION));
        // 初始化策略
        initStrategies(context);
    }


    private void initStrategies(MyApplicationContext context) {
        // 有九种策略
        // 针对每个用户请求，都会经过一些处理策略处理，最终才能有结果输出
        // 每种策略可以自定义干预，但是最终的结果都一致
        /******************这里就是九大组件******************/
//        // 1、多文件上传组件。初始化文件上传解析器，如果请求类型是Mutipart，将通过MutipartResolver进行文件上传解析
//        initMutipartResolver(context);
//
//        // 2、本地语言环境。初始化本地化解析器
//        initLocaleResolver(context);
//
//        // 3、主题模板处理器。初始化主题解析器
//        initThemeResolver(context);

        // ******通过HandlerMapping将请求映射到处理器，HandlerMapping用来保存Controller中配置的RequestMapping和Method的对应关系
        // 4、保存Url映射关系
        initHandlerMappings(context);

        // ******通过HandlerAdapter进行多类型的参数动态匹配，HandlerAdapters用来动态匹配Method参数，包括类转换和动态赋值
        // 5、动态参数适配器
        initHandlerAdapters(context);

        // 如果执行过程中遇到异常，将交给HandlerExceptionResolver来解析
        // 6、异常拦截器
//        initHandlerExceptionResolvers(context);
//
//        // 直接将请求解析到视图名
//        // 7、视图提取器，从request中获取viewName
//        initRequestToViewNameTranslator(context);

        // 通过ViewResolvers实现动态模板解析
        // 自己解析一套模板语言
        // 通过ViewResolvers将逻辑视图解析到具体视图实现
        // 8、视图转换器，模板引擎。

        initViewResolvers(context);

        // Flash映射管理器
        // 9、参数缓存器
//        initFlashMapManager(context);
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

        if (context.getBeanDefinitionCount() == 0) {
            return;
        }

        // 首先从容器中获取所有的beanName
        for (String beanName : context.getBeanDefinitionNames()) {
            // 到了MVC层对外提供的方法只有一个getBean()方法。获取beanName对应的一个实例。
            Object instance = context.getBean(beanName);
            // 反射获取类型类
            Class<?> clazz = instance.getClass();

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
                String regex = (baseUrl + requestMapping.value().replaceAll("\\*", ".").replaceAll("/+", "/"));
                // 保存当前方法的正则模板，便于浏览器访问时做对比
                Pattern pattern = Pattern.compile(regex);

                this.handlerMappings.add(new MyHandlerMapping(instance, method, pattern));
                System.out.println("Mapping: " + regex + "," + method);;
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
        // 从配置文件找到页面模板的根路径
        String templateRoot = context.getContextConfig().getProperty("templateRoot");
        // 获取资源文件的路径
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        // 通过资源文件的路径获取文件夹
        File templateRootDir = new File(templateRootPath);

        for (File template : templateRootDir.listFiles()) {
            // TODO 为什么传templateRoot，应该是template吧？？？
//            this.viewResolvers.add(new MyViewResolver(templateRoot));
            // TODO 应该传入上级文件目录
            this.viewResolvers.add(new MyViewResolver(template));
        }
    }


//    private void initFlashMapManager(MyApplicationContext context) {
//        /**
//         * FlashMap 缓存参数，闪存，不永久的存
//         * request.forward()，自动携带上一次请求的所有参数
//         * response.redirect()，丢失上一次请求的所有参数
//         */
//    }
//
//    private void initRequestToViewNameTranslator(MyApplicationContext context) {
//    }
//
//    private void initHandlerExceptionResolvers(MyApplicationContext context) {
//    }
//
//    private void initThemeResolver(MyApplicationContext context) {
//    }
//
//    private void initLocaleResolver(MyApplicationContext context) {
//    }
//
//    private void initMutipartResolver(MyApplicationContext context) {
//    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatcher(req, resp);
        } catch (Exception e) {
            try {
                System.out.println(e);
                Map<String, Object> model = new HashMap<>();
                model.put("detail", e.getMessage());
                model.put("stackTrace", e.getStackTrace());
                MyModelAndView mv = new MyModelAndView("500", model);
                processDispatcherResult(req, resp, mv);
            } catch (Exception ex) {
                ex.printStackTrace();
                resp.getWriter().write("505, server error!");
            }
        }
    }

    /**
     * 功能描述：处理请求的入口，委派模式，只调度不干活
     * @author ykq
     * @date 2020/5/19 20:56
     * @param
     * @return
     */
    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        // 1、根据用户请求的URL获取一个HandlerMapping
        MyHandlerMapping handler = getHandler(req);

        if (null == handler) {
            // 统一处理返回结果
            processDispatcherResult(req, resp, new MyModelAndView("404"));
            return;
        }

        // 2、适配器模式。根据一个HandlerMapping获得一个HandlerAdapter，动态解析参数。
        MyHandlerAdapter handlerAdapter = getHandlerAdapter(handler);

        // 3、用HandlerAdapter解析某一个方法的返回值后，统一封装成ModelAndView
        MyModelAndView mv = handlerAdapter.handler(req, resp, handler);

        // 4、将mv渲染成一个可以输出的结果--ViewResolver
        processDispatcherResult(req, resp, mv);
    }

    /**
     * 功能描述：Adapter和handler
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
     * 功能描述： 统一输出结果的方法、没有相应的处理器，则生成404页面
     * @author ykq
     * @date 2020/5/19 19:39
     * @param
     * @return
     */
    private void processDispatcherResult(HttpServletRequest req, HttpServletResponse resp, MyModelAndView mv) throws Exception {
        // 调用viewResolver的resolverViewName()方法
        if (null == mv) {
            // 如果是空，则用response输出即可
            return;
        }

        if (this.viewResolvers.isEmpty()) {
            return;
        }

//        if (this.viewResolvers != null) {   不太好，自以为用size>0比较好
//        if (0 < this.viewResolvers.size()) {
        for (MyViewResolver viewResolver : this.viewResolvers) {
            if (viewResolver.getViewName().equals(viewResolver.packAimViewName(mv.getViewName()))){
                MyView view = viewResolver.resolveViewName(mv.getViewName(), null);

                // 往浏览器输出
                if (null != view) {
                    // 渲染
                    view.render(mv.getModel(), req, resp);
                    return;
                }
            }
        }
//        }
    }


    /**
     * 功能描述: 获取对应处理器  TODO 这段要好好看看
     * @author ykq
     * @date 2020/5/23 19:36
     * @param
     * @return com.my.spring.framework.webmvc.MyHandlerMapping
     */
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
