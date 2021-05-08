package com.my.spring.framework.aop;

import com.my.spring.framework.aop.support.MyAdvisedSupport;

/**
 * @ClassName MyDefaultAopProxyFactory
 * @Description 工厂模式
 * @Author ykq
 * @Date 2021/05/07
 * @Version v1.0.0
 */
public class MyDefaultAopProxyFactory {

    public MyAopProxy createAopProxy(MyAdvisedSupport advisedSupport) {
        Class targetClass = advisedSupport.getTargetClass();
        if (targetClass.getInterfaces().length > 0) {
            // 如果有接口用JdkProxy
            return new MyJdkDynamicAopProxy(advisedSupport);
        } else {
            // 否则用cglib
            return new MyCglibAopProxy(advisedSupport);
        }
    }

    public static void main(String[] args) {
        MyAopProxy proxy = new MyCglibAopProxy(null);
        System.out.println(proxy);
    }
}
