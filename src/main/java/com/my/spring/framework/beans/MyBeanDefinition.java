package com.my.spring.framework.beans;

/**
 * @ClassName （配置封装模块）MyBeanDefnition
 * @Description 保存Bean相关的配置信息
 * @Author ykq
 * @Date 2020/5/13
 * @Version v1.0.0
 */
public class MyBeanDefinition {
    /** 原生Bean的全类名 */
    private String beanClassName;

    /** 标记是否延时加载 */
    private boolean lazyInit = false;

    /** 保存beanName，与IoC容器中的key对应 */
    private String factoryBeanName;

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public boolean isLazyInit() {
        return lazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }
}
