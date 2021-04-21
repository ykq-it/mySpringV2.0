package com.my.spring.framework.core;

/**
 * @ClassName （顶层接口模块）MyFactoryBean
 * @Description TODO
 * @Author ykq
 * @Date 2020/5/13
 * @Version v1.0.0
 */
public interface MyFactoryBean<T> {

//    T getObject();
//
//    Class<?> getObjectType();
//
//    boolean isSingleton();
}
/*
 * spring有两种类型的bean，一种是普通的bean，另一种是factoryBean。
 * factoryBean跟普通的bean不同，其返回的对象不是指定类的实例，而是该factoryBean的getObject方法所返回的对象。该对象是否单例，有isSingleton决定。如下
public interface MyFactoryBean<T> {

    T getObject();

    Class<?> getObjectType();

    boolean isSingleton();
}
 */
