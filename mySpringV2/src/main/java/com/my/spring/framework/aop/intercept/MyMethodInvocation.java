package com.my.spring.framework.aop.intercept;

import com.my.spring.framework.aop.aspect.MyJoinPoint;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MyMethodInvocation
 * @Description 注意：为简化开发，才使MyMethodInvocation implements MyJoinPoint
 * @Author ykq
 * @Date 2021/05/08
 * @Version v1.0.0
 */
@Data
public class MyMethodInvocation implements MyJoinPoint {
    // protected后面有用
    protected final Object proxy;
    protected final Object target;
    protected final Method method;
    protected Object[] arguments = new Object[0];

    private final Class<?> targetClass;
    protected final List<?> interceptorsAndDynamicMethodMatchers;

    // 下标
    private int currentInterceptorIndex = -1;

    // 保存自定义属性
    private Map<String, Object> userAttributes = new HashMap<>();

    public MyMethodInvocation(Object proxy, Object target, Method method, Object[] arguments, Class targetClass, List<Object> interceptorsAndDynamicMethodMatchers) {
        this.proxy = proxy;
        this.target = target;
        this.targetClass = targetClass;
        this.method = method;
        this.arguments = arguments;
        this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
    }

    public Object proceed() throws Throwable {
        // 如果Interceptor执行完了，则执行JoinPoint
        if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
            return this.method.invoke(target, arguments);
        } else {
            // 执行链，下标先加再用，获得当前的advice
            Object interceptorOrInterceptionAdvice = this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
            // 判断当前advice是不是拦截器，如果是则调用invoke方法，如果不是则继续递归
            if (interceptorOrInterceptionAdvice instanceof MyMethodInterceptor) {
                MyMethodInterceptor mi = (MyMethodInterceptor)interceptorOrInterceptionAdvice;
//                Class<?> targetClass = this.targetClass != null ? this.targetClass : this.method.getDeclaringClass();
                return mi.invoke(this);
            } else {
                return this.proceed();
            }
        }
    }

    @Override
    public Object getThis() {
        return target;
    }

    @Override
    public void setUserAttribute(String key, Object value) {
        userAttributes.put(key, value);
    }

    @Override
    public Object getUserAttribute(String key) {
        return userAttributes.get(key);
    }
}
