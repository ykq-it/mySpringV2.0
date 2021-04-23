package com.my.spring.framework.context.support;

/**
 * @ClassName （IoC容器模块-支持模块）MyAbstractApplication
 * @Description IoC容器实现类的顶层抽象类，实现IoC容器相关的公共逻辑
 * @Author ykq
 * @Date 2020/5/13
 * @Version v1.0.0
 */
public abstract class MyAbstractApplicationContext {
    /** 只提供给子类重写 */
    public void refresh() throws Exception {}
}
