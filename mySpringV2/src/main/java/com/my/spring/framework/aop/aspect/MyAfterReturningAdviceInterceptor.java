package com.my.spring.framework.aop.aspect;

import com.my.spring.framework.aop.intercept.MyMethodInterceptor;
import com.my.spring.framework.aop.intercept.MyMethodInvocation;

import java.lang.reflect.Method;

/**
 * @ClassName AfterReturningAdviceInterceptor
 * @Description TODO
 * @Author ykq
 * @Date 2021/05/08
 * @Version v1.0.0
 */
public class MyAfterReturningAdviceInterceptor extends MyAbstractAspactJAdvice implements MyMethodInterceptor {

    private MyJoinPoint joinPoint;

    public MyAfterReturningAdviceInterceptor(Object aspectObj, Method adviceMethod) {
        super(aspectObj, adviceMethod);
    }


    @Override
    public Object invoke(MyMethodInvocation mi) throws Throwable {
        joinPoint = mi;
        // 先调proceed
        Object retVal = mi.proceed();
        this.afterReturning(retVal, mi.getMethod(), mi.getArguments(), mi.getThis());
        return retVal;
    }

    private void afterReturning(Object retVal, Method method, Object[] arguments, Object aThis) throws Exception {
        this.invokeAdviceMethod(joinPoint, retVal, null);
    }
}
