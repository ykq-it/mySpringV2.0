package com.my.spring.framework.core;

/**
 * @ClassName （顶层接口模块）MyBeanFactory
 * @Description 单例工厂的顶层设计
 * @Author ykq
 * @Date 2020/5/13
 * @Version v1.0.0
 */
public interface MyBeanFactory {
    /**
     * 功能描述： 根据beanName从IoC中获取一个实例Bean
     * @author ykq
     * @date 2020/5/13 18:36
     * @param
     * @return
     */
    Object getBean(String beanName);

    /**
     * 功能描述： 通过反射从IoC中获取一个实例Bean
     * @author ykq
     * @date 2020/5/13 18:56
     * @param
     * @return
     */
    Object getBean(Class<?> beanClass);
}
