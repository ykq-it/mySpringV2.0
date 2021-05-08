package com.my.spring.framework.aop.aspect;

import com.my.spring.framework.aop.intercept.MyMethodInterceptor;
import com.my.spring.framework.aop.intercept.MyMethodInvocation;

import java.lang.reflect.Method;

/**
 * @ClassName MyMethodBeforeAdviceInterceptor
 * @Description TODO
 * @Author ykq
 * @Date 2021/05/08
 * @Version v1.0.0
 */
public class MyMethodBeforeAdviceInterceptor extends MyAbstractAspactJAdvice implements MyMethodInterceptor {

    private MyJoinPoint joinPoint;

    public MyMethodBeforeAdviceInterceptor(Object aspectObj, Method adviceMethod) {
        super(aspectObj, adviceMethod);
    }

    @Override
    public Object invoke(MyMethodInvocation mi) throws Throwable {
        joinPoint = mi;
        // 调用具体的代码
        this.before(mi.getMethod(), mi.getArguments(), mi.getThis());
        return mi.proceed();
    }

    private void before(Method method, Object[] arguments, Object aThis) throws Exception {
        invokeAdviceMethod(joinPoint,  null, null);
    }
}
