package com.my.spring.framework.context;

import com.my.spring.framework.annotation.MyAutowired;
import com.my.spring.framework.annotation.MyController;
import com.my.spring.framework.annotation.MyService;
import com.my.spring.framework.aop.MyJdkDynamicAopProxy;
import com.my.spring.framework.aop.config.MyAopConfig;
import com.my.spring.framework.aop.support.MyAdvisedSupport;
import com.my.spring.framework.beans.MyBeanWrapper;
import com.my.spring.framework.beans.config.MyBeanDefinition;
import com.my.spring.framework.beans.support.MyBeanDefinitionReader;
import com.my.spring.framework.beans.support.MyDefaultListableBeanFactory;
import com.my.spring.framework.config.MyBeanPostProcessor;
import com.my.spring.framework.context.support.MyAbstractApplicationContext;
import com.my.spring.framework.core.MyBeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName （IoC容器模块）MyApplicationContext--就是一个工厂
 * @Description 直接接触用户的入口，继承DefaultListableBeanFactory实现refresh()，实现BeanFactory实现getBean()。完成IoC、DI、AOP的衔接
 * @Author ykq
 * @Date 2020/5/13
 * @Version v1.0.0
 */
public class MyApplicationContext extends MyAbstractApplicationContext implements MyBeanFactory {

    /** 上下文持有ListableBeanFactory的引用 */
    private MyDefaultListableBeanFactory registry = new MyDefaultListableBeanFactory();

    /** 配置文件的地址 */
    private String[] configLocations;

    /** 配置文件加载和扫描类 */
    private MyBeanDefinitionReader reader;

    /** 单例的<beanName，实例>缓存，（用来保证注册时单例的容器） */
    private Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<>();

    /** 通用的IoC容器，保存的是BeanWrapper（有实例、有类型类），（用来保证注册式单例的容器） */
    private Map<String, MyBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();

    Map<Object, Field> withoutDIInstanceCache = new ConcurrentHashMap<>();

    /** 构造方法 */
    public MyApplicationContext(String... configLocations) {
        this.configLocations = configLocations;

        // 父类空实现，需要复写
        try {
            // 获得配置后，获取BeanDefinition，创建AOP代理实例并注册IoC容器，注入属性
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 功能描述： 重新父类方法
     * @author ykq
     * @date 2020/5/13 21:13
     * @param
     * @return
     */
    @Override
    public void refresh() throws Exception {
        // 1、定位配置文件，保存配置信息，以及扫描（遍历）得到类的全类名
        reader = new MyBeanDefinitionReader(this.configLocations);

        // 2、加载配置文件，扫描相关的类，把它们封装成BeanDefinition的List
        List<MyBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

        // 3、注册，把配置信息放到缓存容器里面
        registry.doRegisterBeanDefinition(beanDefinitions);

        // 4、初始化非延迟加载的类，并完成自动依赖注入 TODO 注入是不是有问题
        doLoadInstance();
    }

    /**
     * 功能描述： 初始化Bean的入口
     * @author ykq
     * @date 2020/5/14 19:41
     * @param
     * @return
     */
    private void doLoadInstance() {
        for (Map.Entry<String, MyBeanDefinition> beanDefinitionEntry : registry.beanDefinitionMap.entrySet()) {
            String factoryBeanName = beanDefinitionEntry.getKey();
            if (!registry.beanDefinitionMap.get(factoryBeanName).isLazyInit()) {

                // 从这里开始依赖注入，读取BeanDefinition中的信息，通过反射创建实例并返回。
                // Spring不会直接将实例放入IoC容器，而是BeanWrapper，目的是为了以后的代理。
                // 装饰器模式，1、保留原来的OOP关系；2、可以支持代理扩展
                System.out.println( "getBean: " + factoryBeanName);
                getBean(factoryBeanName);
            }
        }

        while (this.withoutDIInstanceCache.size() > 0) {
            for (Map.Entry<Object, Field> objectFieldEntry : this.withoutDIInstanceCache.entrySet()) {
                try {
                    populateBean(null, objectFieldEntry.getKey());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    /***
     * 功能描述: 用类型类getBean时，用装饰者模式调用传参beanName
     * @author ykq
     * @date 2020/5/23 14:10
     * @param
     * @return java.lang.Object
     */
    @Override
    public Object getBean(Class<?> beanClass) {
        // TODO 这样可不可以
        return getBean(beanClass.getName());
    }


    /**
     * 功能描述： 完成IoC和DI的入口
     * @author ykq
     * @date 2020/5/14 19:57
     * 依赖注入从这里开始，通过读取BeanDefinition中的信息，然后通过反射机制创建一个实例并返回
     * Spring中，不会吧最原始的对象放出去，会用用BeanWrapper进行一次包装
     * 装饰器模式：
     *  1、保留原来的OOP关系
     *  2、需要对它进行扩展，增强（为以后的AOP打基础）
     */
    @Override
    public Object getBean(String factoryBeanName) {
        // 1、获取当前beanName的bean定义
        MyBeanDefinition beanDefinition = registry.beanDefinitionMap.get(factoryBeanName);

        // 生成通知事件
        MyBeanPostProcessor beanPostProcessor = new MyBeanPostProcessor();

        // 2、通过BeanDefinition创建一个真正的实例，反射实例化
        Object instance = instantiateBean(beanDefinition);
        if (null == instance) {
            return null;
        }

        try {
            // 调用bean前处理器
            beanPostProcessor.postProcessorBeforeInitialization(instance, factoryBeanName);

            // 3、封装BeanWrapper对象
            MyBeanWrapper beanWrapper = new MyBeanWrapper(instance);

            // 4、保存IoC容器BeanWrapper，完成IoC注册
            factoryBeanInstanceCache.put(factoryBeanName, beanWrapper);

            // 调用bean后处理器
            beanPostProcessor.postProcessorAfterInitialization(instance, factoryBeanName);

            // 5、DI，属性完成注入
            populateBean(factoryBeanName, instance);

            return factoryBeanInstanceCache.get(factoryBeanName).getWrappedInstance();
        } catch (Exception e) {
            // TODO DemoAction之所以可以重复遍历，是不是跟return null有关。
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 功能描述： 完成Controller、Service修饰的类中，被Autowired修饰的属性的注入
     * @author ykq
     * @date 2020/5/14 20:46
     * @param
     * @return
     */
    // TODO 循环依赖怎么做。用两个缓存。1、把第一次循环读取结果是空的BeanDefinition存到第一个缓存。2、等第一次循环之后，第二次检查第一次的缓存，再进行赋值。或者用递归
    private void populateBean(String beanName, Object instance) throws IllegalAccessException {
        Class clazz = instance.getClass();

        // TODO 目前只给controller、service注解的类注入，日后扩展resource、component等等。controller等等都是component的子类
        if (!(clazz.isAnnotationPresent(MyController.class) || clazz.isAnnotationPresent(MyService.class))) {
            return;
        }

        // getFields()：获得某个类的所有的公共（public）的字段，包括父类中的字段。
        // getDeclaredFields()：获得某个类的所有声明的字段，即包括public、private和protected，但是不包括父类的申明字段。
        // TODO 看一下源码此处是如何处理的
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(MyAutowired.class)) {
                continue;
            }

            // 授权，强制访问
            field.setAccessible(true);
            // 得到此属性的值
            Object fieldValue = field.get(instance);
            if (null == fieldValue) {
                MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                String autowiredBeanName = autowired.value().trim();
                if ("".equals(autowiredBeanName)) {
                    // 这里应该取类名，而非全类名
                    // autowiredBeanName = toLowerFirstCase(field.getType().getName());
                    autowiredBeanName = toLowerFirstCase(field.getType().getSimpleName());
                }

                try {
                    if (null == this.factoryBeanInstanceCache.get(autowiredBeanName)) {
                        withoutDIInstanceCache.put(instance, field);
                        continue;
                    }
                    field.set(instance, this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappedInstance());
                    withoutDIInstanceCache.remove(instance, field);
                } catch (Exception e) {
                    e.printStackTrace();
                    // 如果发生异常或者容器中没有就继续
                    continue;
                }
            }
        }

    }

    /**
     * 功能描述： 创建真正的实例
     * @author ykq
     * @date 2020/5/14 20:07
     * @param
     * @return
     */
    private Object instantiateBean(MyBeanDefinition beanDefinition) {
        Object instance = null;
        String beanName = beanDefinition.getFactoryBeanName();
        String className = beanDefinition.getBeanClassName();

        // TODO 接口不能创建对象
        try {
            // 优化：先判断实例map缓存，是否已经生成过当前类型类的实例
            if (factoryBeanObjectCache.containsKey(className)) {
                // 保证单例
                instance = factoryBeanObjectCache.get(className);
            } else {
                Class clazz = Class.forName(className);
                instance = clazz.newInstance();

                /************************AOP开始***********************/
                // 1、加载AOP的配置文件
                // TODO 岂不是每个生成实例的对象都会获得一遍AdvisedSupport? 好像是的，每个类对应的AopConfig或许不同
                MyAdvisedSupport advisedSupport = instantionAopConfig(beanDefinition);
                advisedSupport.setTargetClass(clazz);  // 赋值的同时，解析并为各方法创建增强点和编织增强方法的映射
                advisedSupport.setTarget(instance);

                // 判断规则，要不要生成代理类，如果要就覆盖原生对象。如果不要就不做任何处理，返回原生对象
                if (advisedSupport.pointCutMatch()) {
                    // Method threw 'java.lang.NullPointerException' exception. Cannot evaluate com.sun.proxy.$Proxy5.toString()
                    instance = new MyJdkDynamicAopProxy(advisedSupport).getProxy();
                }
                /************************AOP结束***********************/

                factoryBeanObjectCache.put(className, instance);
                factoryBeanObjectCache.put(beanDefinition.getFactoryBeanName(), instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    /**
     * 功能描述： 
     * @author ykq
     * @date 2020/5/27 19:17
     * @param 
     * @return 
     */
    private MyAdvisedSupport instantionAopConfig(MyBeanDefinition beanDefinition) {
        // 解析配置文件获取aop相关的配置
        MyAopConfig aopConfig = new MyAopConfig();
        // 需要编织的类和方法符合的条件
        aopConfig.setPointCut(this.reader.getContextConfig().getProperty("pointCut"));
        // 切面类、需要织入的类
        aopConfig.setAspectClass(this.reader.getContextConfig().getProperty("aspectClass"));
        // 前置的方法名
        aopConfig.setAspectBefore(this.reader.getContextConfig().getProperty("aspectBefore"));
        // 后置的方法名
        aopConfig.setAspectAfter(this.reader.getContextConfig().getProperty("aspectAfter"));
        // 异常通知回调方法
        aopConfig.setAspectAfterThrow(this.reader.getContextConfig().getProperty("aspectAfterThrow"));
        // 异常类型捕获
        aopConfig.setAspectAfterThrowingName(this.reader.getContextConfig().getProperty("aspectAfterThrowingName"));
        return new MyAdvisedSupport(aopConfig);
    }


    /***
     * 功能描述: 获取beanDefinitionMap所有的key
     * @author ykq
     * @date 2020/5/23 14:07
     * @param
     * @return java.lang.String[]
     */
    public String[] getBeanDefinitionNames() {
        return registry.beanDefinitionMap.keySet().toArray(new String[registry.beanDefinitionMap.size()]);
    }


    /***
     * 功能描述: 获取beanDefinitionMap中的数量
     * @author ykq
     * @date 2020/5/23 14:07
     * @param
     * @return int
     */
    public int getBeanDefinitionCount() {
        return registry.beanDefinitionMap.size();
    }


    /***
     * 功能描述: 获取配置文件转成的Properties
     * @author ykq
     * @date 2020/5/23 14:09
     * @param
     * @return java.util.Properties
     */
    public Properties getContextConfig() {
        return this.reader.getContextConfig();
    }


    /**
     * 功能描述： 获取首字母小写的beanName
     * @author ykq
     * @date 2020/5/13 21:29
     * @param
     * @return
     */
    private String toLowerFirstCase(String simpleName) {
        char[] c = simpleName.toCharArray();
        c[0] += 32;
        return String.valueOf(c);
    }
}
