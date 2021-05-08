package com.my.spring.framework.aop.aspect;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * @ClassName MyAbstractAspactJAdvice
 * @Description TODO
 * @Author ykq
 * @Date 2021/05/08
 * @Version v1.0.0
 */
@Data
public class MyAbstractAspactJAdvice {

    // 调用方法的底层队形
    private Object aspectObj;

    private Method adviceMethod;

    private String throwName;

    public MyAbstractAspactJAdvice(Object aspect, Method adviceMethod) {
        this.aspectObj = aspect;
        this.adviceMethod = adviceMethod;
    }

    protected Object invokeAdviceMethod(MyJoinPoint joinPoint, Object returnValue, Throwable ex) throws Exception {
        Class<?> [] paramTypes = adviceMethod.getParameterTypes();
        if (paramTypes.length == 0) {
            return adviceMethod.invoke(aspectObj);
        } else {
            // Spring的规范
            Object[] args = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length ; i++) {
                if (paramTypes[i] == MyJoinPoint.class) {
                    args[i] = joinPoint;
                } else if (paramTypes[i] == Throwable.class) {
                    args[i] = ex;
                } else if (paramTypes[i] == Object.class) {
                    args[i] = returnValue;
                }
            }
            return adviceMethod.invoke(aspectObj, args);
        }
    }
}
