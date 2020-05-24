package com.my.spring.framework.webmvc;

import com.my.spring.framework.annotation.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName MyHandlerAdapter
 * @Description TODO 适配器模式，将Request的字符型参数自动适配为Method的实参，主要实现参数列表自动适配和类型转换功能
 * @Author ykq
 * @Date 2020/5/18
 * @Version v1.0.0
 */
public class MyHandlerAdapter {

    // TODO 有意义吗？本来传的就是HandlerMapping的对象
    public boolean supports(Object handler) {
        return (handler instanceof MyHandlerMapping);
    }

    /***
     * 功能描述: 解析某一个放的形参和返回值之后，统一封装为ModelAndView对象
     * @author ykq
     * @date 2020/5/24 12:40
     * @param
     * @return com.my.spring.framework.webmvc.MyModelAndView
     */
    public MyModelAndView handler(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        MyHandlerMapping handlerMapping = (MyHandlerMapping) handler;

        // 1、封装形参列表，分类讨论：有注解的，没注解的。
        // 每个方法都有一个参数列表，这里保存的是形参列表中参数和位置的关系
        Map<String, Integer> paramIndexMapping = new HashMap<>();

        // 这里只给出命名参数。 TODO 这里为什么是二维数组，因为一个值可以被多个注解修饰，debug看一下
        Annotation[][] pa = handlerMapping.getMethod().getParameterAnnotations();
        for (int i = 0; i < pa.length; i++) {
            for (Annotation a : pa[i]) {
                if (a instanceof MyRequestParam) {
                    String paramName = ((MyRequestParam) a).value();
                    if (!"".equals(paramName.trim())) {
                        paramIndexMapping.put(paramName, i);
                    }
                }
            }
        }
        // req和resp没有注解，所以没有name，可以用全 类名
        Class<?>[] paramTypes = handlerMapping.getMethod().getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            Class paramType = paramTypes[i];
            if (HttpServletRequest.class == paramType || HttpServletResponse.class == paramType) {
                paramIndexMapping.put(paramType.getName(), i);
            }
        }

        // 2、根据用户请求的参数信息，跟Method中的参数信息进行动态匹配。分类讨论：用户传参、req和resp
        // 从req里获取入参。为什么value是数组：一个key可能有多个值，比如http://ip:port/web/query?name=cat&dog
        Map<String, String[]> params = req.getParameterMap();

        // 真正的参数是不同的类型，Integer、String等等，所以用Object保存实参的值
        Object[] paramValue = new Object[paramTypes.length];

        for (Map.Entry<String, String[]> param: params.entrySet()) {
            // TODO 初期简答的将数组转换成一个String，日后再拓展
            String value = Arrays.toString(params.get(param.getKey())).replaceAll("\\[|\\]", "")
                    .replaceAll("\\s", ",");

            if (!paramIndexMapping.containsKey(param.getKey())) {
                continue;
            }

            int index = paramIndexMapping.get(param.getKey());
            // 将value转换成形参对应的类型，并按照形参的下标保存实参的值。 TODO 允许自定义类型转换器Converter
            paramValue[index] = castStringValue(value, paramTypes[index]);
        }

        if (paramIndexMapping.containsKey(HttpServletResponse.class.getName())) {
            paramValue[paramIndexMapping.get(HttpServletResponse.class.getName())] = req;
        }
        if (paramIndexMapping.containsKey(HttpServletRequest.class.getName())) {
            paramValue[paramIndexMapping.get(HttpServletRequest.class.getName())] = resp;
        }

        // 调用方法。obj - 从底层方法被调用的对象.args - 用于方法调用的参数
        Object result = handlerMapping.getMethod().invoke(handlerMapping.getController(), paramValue);
        // TODO 如果用了ResponseBody，以后扩展
        if (null == result || result instanceof Void) {
            return null;
        }

        boolean isModelAndView = handlerMapping.getMethod().getReturnType() == MyModelAndView.class;
        if (isModelAndView) {
            return (MyModelAndView) result;
        }

        return null;
    }

    /***
     * 功能描述: 将req中取的不分类型的值，按照形参的类型转换  TODO 扩展
     * @author ykq
     * @date 2020/5/24 13:49
     * @param
     * @return java.lang.Object
     */
    private Object castStringValue(String value, Class<?> clazz) {
        if (String.class == clazz) {
            return value;
        } else if (Integer.class == clazz) {
            return Integer.valueOf(value);
        } else {
            // TODO 自定义类型处理
            return null;
        }
    }
}
