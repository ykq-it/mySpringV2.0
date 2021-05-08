package com.my.spring.framework.aop.aspect;

import java.lang.reflect.Method;

/**
 * @ClassName MyJoinPoint
 * @Description TODO
 * @Author ykq
 * @Date 2021/05/08
 * @Version v1.0.0
 */
public interface MyJoinPoint {
    // 当前要增强的目标类的实例
    Object getThis();

    // 获取参数列表
    Object[] getArguments();

    // 获取方法
    Method getMethod();

    void setUserAttribute(String key, Object value);

    Object getUserAttribute(String key);
}
