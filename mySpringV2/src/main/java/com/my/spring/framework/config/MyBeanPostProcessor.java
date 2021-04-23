package com.my.spring.framework.config;

/**
 * @ClassName MyBeanPostProcessor
 * @Description TODO Spring中的BeanPostProcessor是为对象初始化事件而设置的一种回调机制。此处只说明不，不实现。
 * @Author ykq
 * @Date 2020/5/14
 * @Version v1.0.0
 */
public class MyBeanPostProcessor {

    /**
     * 功能描述： 为bean初始化之前，提供回调入口（bean前处理器）
     * @author ykq
     * @date 2020/5/14 20:02
     * @param
     * @return
     */
    public Object postProcessorBeforeInitialization(Object bean, String beanName) throws Exception {
        return bean;
    }

    /**
     * 功能描述： 为bean初始化之后，提供回调入口（bean后处理器）
     * @author ykq
     * @date 2020/5/14 20:03
     * @param
     * @return
     */
    public Object postProcessorAfterInitialization(Object bean, String beanName) throws Exception {
        return bean;
    }
}
