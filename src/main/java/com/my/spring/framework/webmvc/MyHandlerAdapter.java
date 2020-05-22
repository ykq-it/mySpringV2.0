package com.my.spring.framework.webmvc;

import com.my.spring.framework.annotation.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
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

    public MyModelAndView handler(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        MyHandlerMapping handlerMapping = (MyHandlerMapping) handler;

        // 每个方法都有一个参数列表，这里保存的是形参列表
        Map<String, Integer> paramMapping = new HashMap<>();

        // 这里只给出命名参数
        Annotation[][] pa = handlerMapping.getMethod().getParameterAnnotations();
        for (int i = 0; i < pa.length; i++) {
            for (Annotation a : pa[i]) {
                if (a instanceof MyRequestParam) {
                    String paramName = ((MyRequestParam) a).value();
                    if (!"".equals(paramName.trim())) {
                        paramMapping.put(paramName, i);
                    }
                }
            }
        }

        // 根据用户请求的参数信息，跟Method中的参数信息进行动态匹配

        return null;
    }


    private Object caseStringValue(String value, Class<?> clazz) {
        return null;
    }
}
