package com.my.spring.framework.aop.aspect;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * @ClassName MyAdvice
 * @Description 当被编织的方法执行时，用来执行被增强的部分。原理利用反射，adviceMethod.invoke(aspect)。每一个方法的各切点分别有一个
 * @Author ykq
 * @Date 2020/5/27
 * @Version v1.0.0
 */
@Data
public class MyAdvice {
    /** 切面类的实例 */
    private Object aspect;
    /** 切面类的某个方法 */
    private Method adviceMethod;
    /** 当方法是异常后通知方法，此处则为配置文件定义的抛出的异常名称 */
    private String throwName;

    public MyAdvice(Object aspect, Method adviceMethod) {
        this.aspect = aspect;
        this.adviceMethod = adviceMethod;
    }
}
