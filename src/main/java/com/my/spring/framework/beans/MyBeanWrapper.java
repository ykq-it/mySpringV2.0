package com.my.spring.framework.beans;

/**
 * @ClassName （配置封装模块）MyBeanWrapper
 * @Description TODO
 * @Author ykq
 * @Date 2020/5/13
 * @Version v1.0.0
 */
public class MyBeanWrapper {

    // TODO 为什么保存了实例，又保存对象？
    /** 对象实例 */
    private Object wrapperInstance;

    /** 某一类型类，对wrappedInstance的代理类型 */
    private Class<?> wrappedClass;

    public MyBeanWrapper(Object wrapperInstance) {
        this.wrapperInstance = wrapperInstance;
        this.wrappedClass = wrapperInstance.getClass();
    }

    public Object getWrapperInstance() {
        return this.wrapperInstance;
    }

    /**
     * 功能描述： 返回类型类，比如返回代理以后的Class
     * @author ykq
     * @date 2020/5/13 19:47
     * @param
     * @return
     */
    public Class<?> getWrappedClass() {
        return this.wrapperInstance.getClass();
    }
}
