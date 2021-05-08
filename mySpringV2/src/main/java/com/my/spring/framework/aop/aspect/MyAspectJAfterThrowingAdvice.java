package com.my.spring.framework.aop.aspect;

import com.my.spring.framework.aop.intercept.MyMethodInterceptor;
import com.my.spring.framework.aop.intercept.MyMethodInvocation;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * @ClassName MyAspectJAfterThrowingAdvice
 * @Description TODO
 * @Author ykq
 * @Date 2021/05/08
 * @Version v1.0.0
 */
@Data
public class MyAspectJAfterThrowingAdvice extends MyAbstractAspactJAdvice implements MyMethodInterceptor {

    private MyJoinPoint joinPoint;

    private String throwName;

    public MyAspectJAfterThrowingAdvice(Object aspectObj, Method adviceMethod) {
        super(aspectObj, adviceMethod);
    }

    @Override
    public Object invoke(MyMethodInvocation mi) throws Throwable {
        joinPoint = mi;
        try {
            return mi.proceed();
        } catch (Throwable var3) {
            this.invokeAdviceMethod(joinPoint, null, var3);
            throw var3;
        }
    }
}
