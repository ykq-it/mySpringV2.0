package com.my.spring.framework.aop.intercept;

/**
 * @ClassName MyMethodInterceptor
 * @Description 前置后置环绕通知都可以实现这个接口，实现动态扩展
 * @Author ykq
 * @Date 2021/05/08
 * @Version v1.0.0
 */
public interface MyMethodInterceptor {

    Object invoke(MyMethodInvocation invocation) throws Throwable;
}
