package com.my.spring.framework.aop;

import com.my.spring.framework.aop.aspect.MyAdvice;
import com.my.spring.framework.aop.support.MyAdvisedSupport;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @ClassName MyCglibAopProxy
 * @Description TODO
 * @Author ykq
 * @Date 2021/05/07
 * @Version v1.0.0
 */
public class MyCglibAopProxy implements MyAopProxy, MethodInterceptor {

    private MyAdvisedSupport advisedSupport;

    public MyCglibAopProxy(MyAdvisedSupport advisedSupport) {
        this.advisedSupport = advisedSupport;
    }

    @Override
    public Object getProxy() {
        // cglib生成字节码的工具
        Enhancer enhancer = new Enhancer();
        // 相当于代理类继承父类clazz
        enhancer.setSuperclass(advisedSupport.getTargetClass());
        enhancer.setCallback(this);
        return enhancer.create();
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return null;
    }

    /** intercept方式的4个参数分别对应增强对象、调用方法、方法参数以及调用父类方法的代理 */
    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        System.out.println("CGlib Proxy!!!");
        Map<String, MyAdvice> advices = advisedSupport.getAdvices(method);
        Object returnValue;

        try {
            invokeAdvice(advices.get("before"));

            returnValue = methodProxy.invokeSuper(o, args);

            invokeAdvice(advices.get("after"));
        }catch (Exception e){
            invokeAdvice(advices.get("afterThrow"));
            throw e;
        }
        return returnValue;
    }

    private void invokeAdvice(MyAdvice advice) {
        try {
            // 对带有指定参数的指定对象调用由此 Method 对象表示的底层方法。个别参数被自动解包，以便与基本形参相匹配，基本参数和引用参数都随需服从方法调用转换。
            advice.getAdviceMethod().invoke(advice.getAspect());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
