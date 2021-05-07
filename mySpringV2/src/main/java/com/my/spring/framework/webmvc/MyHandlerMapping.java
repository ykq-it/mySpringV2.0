package com.my.spring.framework.webmvc;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * @ClassName MyHandlerMapping
 * @Description 策略模式，用输入URL间接调用不同的Method以达到获取结果的目的
 * @Author ykq
 * @Date 2020/5/18
 * @Version v1.0.0
 */
public class MyHandlerMapping {

    /** 目标方法所在的controller对象 */
    private Object controller;

    /** URL对应的目标方法 */
    private Method method;

    /** URL的封装 */
    private Pattern pattern;

    public MyHandlerMapping(Object controller, Method method, Pattern pattern) {
        this.controller = controller;
        this.method = method;
        this.pattern = pattern;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }
}
