package com.my.spring.framework.beans.support;

import com.my.spring.framework.beans.config.MyBeanDefinition;
import com.my.spring.framework.core.MyBeanFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName （IoC容器模块-支持模块）MyDefaultListableBeanFactory
 * @Description DefaultListableBeanFactory是众多IoC容器子类的典型代表。定义顶层的IoC缓存（一个Map），属性名字叫beanDefinitionMap
 * @Author ykq
 * @Date 2020/5/13
 * @Version v1.0.0
 */
public class MyDefaultListableBeanFactory implements MyBeanFactory  {

    /** 存储Bean的配置信息映射--BeanDefinition */
    public final Map<String, MyBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    @Override
    public Object getBean(String beanName) {
        return null;
    }

    @Override
    public Object getBean(Class<?> beanClass) {
        return null;
    }

    /**
     * 功能描述： 通过生成的BeanDefinitionList将bean的定义放到Map中。（注册到父类的beanDefinitionMap）
     * @author ykq
     * @date 2020/5/14 19:18
     * @param
     * @return
     */
    public void doRegisterBeanDefinition(List<MyBeanDefinition> beanDefinitions) throws Exception {
        // BeanDefinition有factoryBeanName、className
        for (MyBeanDefinition beanDefinition : beanDefinitions) {
            // 父类的beanDefinitionMap是以factoryBeanName作为key，如果在注册之前map中已经有这个key，为了确保Bean的name唯一，则抛异常
            // TODO 如果要用全类名做key，此处也要校验是否全类名对应的value是否已存在
            if (beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("The " + beanDefinition.getFactoryBeanName() + " is exist!!");
            }
//            if (beanDefinitionMap.containsKey(beanDefinition.getBeanClassName())) {
//                throw new Exception("The " + beanDefinition.getBeanClassName() + " is exist!!");
//            }

            // key可以不同，但定义是单例的
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
//            beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
//            super.beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
            // 到此为止容器初始化完毕。
            // 总结一下都做了什么？
            // 1、传入locations，获取locations的scanPackage。
            // 2、生成文件目录，遍历.class的文件，保存其全类名到list。
            // 3、初始化BeanDefinitionList，遍历全类名List。a:保存class的factoryBeanName和全路径；b:保存interface的factoryBeanName和其实现类的全路径。
            // 4、遍历BeanDefinitionList，将BeanDefinition的factoryBeanName作为key，BeanDefinition作为value，赋值给DefaultListableBeanFactory的beanDefinitionMap。
        }
    }

}
