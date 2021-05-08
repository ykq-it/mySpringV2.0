package com.my.spring.framework.aop;

/**
 * @ClassName MyAopProxy
 * @Description TODO
 * @Author ykq
 * @Date 2021/05/07
 * @Version v1.0.0
 */
public interface MyAopProxy {

    Object getProxy();

    Object getProxy(ClassLoader classLoader);
}
