package com.my.spring.framework.beans;

/**
 * @ClassName （配置封装模块）MyBeanWrapper
 * @Description 保存Bean的封装信息
 * @Author ykq
 * @Date 2020/5/13
 * @Version v1.0.0
 */
public class MyBeanWrapper {

    // TODO 为什么保存了实例，又保存类型类?
    /** 对象实例 */
    private Object wrappedInstance;

    /** 某一类型类，对wrappedInstance的代理类型
     *   这个类有可能是包装后的$Proxy0，也可能是原生的
     */
    private Class<?> wrappedClass;

    public MyBeanWrapper(Object wrapperInstance) {
        this.wrappedInstance = wrapperInstance;
        this.wrappedClass = wrapperInstance.getClass();
    }

    public Object getWrappedInstance() {
        return this.wrappedInstance;
    }

    /**
     * 功能描述： 返回类型类，比如返回代理以后的Class
     * @author ykq
     * @date 2020/5/13 19:47
     * @param
     * @return
     */
    public Class<?> getWrappedClass() {
        return this.wrappedInstance.getClass();
    }
}
