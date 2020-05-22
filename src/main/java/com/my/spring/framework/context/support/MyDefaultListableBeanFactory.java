package com.my.spring.framework.context.support;

import com.my.spring.framework.beans.config.MyBeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName （IoC容器模块-支持模块）MyDefaultListableBeanFactory
 * @Description DefaultListableBeanFactory是众多IoC容器子类的典型代表。定义顶层的IoC缓存（一个Map），属性名字叫beanDefinitionMap
 * @Author ykq
 * @Date 2020/5/13
 * @Version v1.0.0
 */
public class MyDefaultListableBeanFactory extends MyAbstractApplicationContext {

    /** 存储Bean的配置信息映射--BeanDefinition */
    protected final Map<String, MyBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
}
