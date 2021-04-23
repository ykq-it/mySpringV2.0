package com.my.spring.framework.aop;

import com.my.spring.framework.aop.aspect.MyAdvice;
import com.my.spring.framework.aop.support.MyAdvisedSupport;
import lombok.Data;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @ClassName MyJdkDynamicAopProxy
 * @Description 目标类的动态代理类模板
 * @Author ykq
 * @Date 2020/5/27
 * @Version v1.0.0
 */
@Data
public class MyJdkDynamicAopProxy implements InvocationHandler {
    private MyAdvisedSupport advisedSupport;

    public MyJdkDynamicAopProxy(MyAdvisedSupport advisedSupport) {
        this.advisedSupport = advisedSupport;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Map<String, MyAdvice> advices = advisedSupport.getAdvices(method, null);

        Object returnValue;
        try {
            invokeAdivce(advices.get("before"));

            returnValue = method.invoke(this.advisedSupport.getTarget(),args);

            invokeAdivce(advices.get("after"));
        }catch (Exception e){
            invokeAdivce(advices.get("afterThrow"));
            throw e;
        }
        return returnValue;
    }

    private void invokeAdivce(MyAdvice advice) {
        try {
            // 对带有指定参数的指定对象调用由此 Method 对象表示的底层方法。个别参数被自动解包，以便与基本形参相匹配，基本参数和引用参数都随需服从方法调用转换。
            advice.getAdviceMethod().invoke(advice.getAspect());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 功能描述： 获得代理类
     * @author ykq
     * @date 2020/5/27 20:31
     * @param
     * @return
     */
    public Object getProxy() {
        // 因为JDK实现动态代理业务的时候，只能针对接口进行代理。
//        return Proxy.newProxyInstance(this.getClass().getClassLoader(), Object.class.getInterfaces(), this);
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), this.advisedSupport.getTargetClass().getInterfaces(), this);
    }
}
