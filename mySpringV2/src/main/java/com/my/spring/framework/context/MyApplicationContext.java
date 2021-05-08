package com.my.spring.framework.context;

import com.my.spring.framework.annotation.MyAutowired;
import com.my.spring.framework.annotation.MyController;
import com.my.spring.framework.annotation.MyService;
import com.my.spring.framework.aop.MyDefaultAopProxyFactory;
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
import java.util.*;
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

    /** 代理工厂，简单策略模式 */
    private MyDefaultAopProxyFactory proxyFactory = new MyDefaultAopProxyFactory();

    /** 配置文件的地址 */
    private String[] configLocations;

    /** 配置文件加载和扫描类 */
    private MyBeanDefinitionReader reader;

    /** 标记，创建过的所有的BeanName，循环依赖的标识，标记当前正在创建的BeanName */
    private Set<String> singletonCurrentlyInCreation = new HashSet<>();

    /** 一级缓存，已经完成依赖注入的Bean，成熟的Bean，所有的，包含各种类型的beanName（eg.类名，自定义等等） */
    private Map<String, Object> singletonObjects = new ConcurrentHashMap<>();

    /** 二级缓存，早期的纯净Bean，只存正常配置的beanName */
    private Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>();

    /** 三级缓存，通用的IoC容器，保存的是BeanWrapper（有实例、有类型类），（用来保证注册式单例的容器） */
    private Map<String, MyBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();

    /** 单例的<beanName，实例>缓存，（用来保证注册时单例的容器） */
    private Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<>();

    /**************************循环依赖思路1***************************/
    Map<MyBeanWrapper, Field> withoutDIInstanceCache = new ConcurrentHashMap<>();
    /**************************循环依赖思路1***************************/

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

    /**  TODO 验证一下代理时是否正常
     * 解决循环依赖的两种思路
     * 1、树的广度优先搜索（BFS）：前提在注入时是从factoryBeanInstanceCache拿到当前属性对应的实例，
     * 因此在第一轮注入时，把拿不到对应属性的实例的BeanWrapper缓存起来，
     * 由于在第一轮注入时，第一层Bean都被创建，且Bean之间的调用是引用调用
     * 所以第二轮根据上面所缓存的未完成注入的BeanWrapper再进行一次注入，
     * 此时factoryBeanInstanceCache便能拿到所有的一层Bean的实例
     * 2、树的深度优先搜索（DFS）：Spring原生的方式，前提在注入时，不是从factoryBeanInstanceCache里拿对应属性的实例，
     * 而是通过继续getBean的方式，这样的好处是，在每一次的最外层getBean都能拿到注入完整的Bean。
     * 在创建最外层Bean时，先把Bean缓存到一级缓存，如果出现循环依赖，则在field.set时再次调用getBean创建里层Bean，
     * 接着会把里层bean也缓存到一级缓存。
     * 当里层Bean需要field.set外层bean时，由于一级缓存已存在外层Bean，则里层Bean完整创建。
     * 此时里层递归结束，返回到外层递归。外层获取到完整的里层Bean，因此外层Bean创建完成，递归结束。
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

        /**************************循环依赖思路1（mine）***************************/
        /*while (withoutDIInstanceCache.size() > 0) {
            for (Map.Entry<MyBeanWrapper, Field> objectFieldEntry : this.withoutDIInstanceCache.entrySet()) {
                try {
                    populateBean(null, null, objectFieldEntry.getKey());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }*/
        /**************************循环依赖思路1***************************/
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

        /**************************循环依赖思路2***************************/
        // 从一级缓存去拿，成熟的Bean
        Object singleton = getSingleton(factoryBeanName, beanDefinition);
        if (null != singleton) {
            // 如果一级缓存中有成熟的Bean，则直接返回
            return singleton;
        }

        if (!singletonCurrentlyInCreation.contains(factoryBeanName)) {
            // 如果当前BeanName不曾添加过创建标记，则标记
            singletonCurrentlyInCreation.add(factoryBeanName);
        }
        /**************************循环依赖思路2***************************/


        // 生成通知事件
//        MyBeanPostProcessor beanPostProcessor = new MyBeanPostProcessor();

        // 2、通过BeanDefinition创建一个真正的实例，反射实例化
        Object instance = instantiateBean(factoryBeanName, beanDefinition);
        if (null == instance) {
            return null;
        }

        /**************************循环依赖思路2***************************/
        // 缓存到一级缓存
        singletonObjects.put(factoryBeanName, instance);
        /**************************循环依赖思路2***************************/

        try {
            // 调用bean前处理器
//            beanPostProcessor.postProcessorBeforeInitialization(instance, factoryBeanName);

            // 3、封装BeanWrapper对象
            MyBeanWrapper beanWrapper = new MyBeanWrapper(instance);

            // 4、DI，属性完成注入。4、5的先后位置没有关系，因为是引用。
            populateBean(factoryBeanName, beanDefinition, beanWrapper);

            // 5、保存IoC容器BeanWrapper，完成IoC注册
            factoryBeanInstanceCache.put(factoryBeanName, beanWrapper);

            // 调用bean后处理器
//            beanPostProcessor.postProcessorAfterInitialization(instance, factoryBeanName);

            return beanWrapper.getWrappedInstance();
        } catch (Exception e) {
            // TODO DemoAction之所以可以重复遍历，是不是跟return null有关。
            e.printStackTrace();
            return null;
        }
    }

    private Object getSingleton(String factoryBeanName, MyBeanDefinition beanDefinition) {
        // 先去一级缓存拿
        Object bean = singletonObjects.get(factoryBeanName);

        // 如果一级缓存没有，但又有创建标识，说明是循环依赖
        if (null == bean && singletonCurrentlyInCreation.contains(factoryBeanName)) {
            bean = earlySingletonObjects.get(factoryBeanName);

            if (null == bean) {
                // 如果二级缓存也没有，则从三级缓存中拿
                bean = instantiateBean(factoryBeanName, beanDefinition);
                // 将创建出来的对象，放入二级缓存中
                earlySingletonObjects.put(factoryBeanName, bean);
            }
        }

        return bean;
    }

    /**
     * 功能描述： 完成Controller、Service修饰的类中，被Autowired修饰的属性的注入
     * @author ykq
     * @date 2020/5/14 20:46
     * @param
     * @param beanDefinition
     * @return
     */
    // TODO 循环依赖怎么做。用两个缓存。1、把第一次循环读取结果是空的BeanDefinition存到第一个缓存。2、等第一次循环之后，第二次检查第一次的缓存，再进行赋值。或者用递归
    private void populateBean(String beanName, MyBeanDefinition beanDefinition, MyBeanWrapper beanWrapper) throws IllegalAccessException {
        Object instance = beanWrapper.getWrappedInstance();
        Class clazz = beanWrapper.getWrappedClass();

        // TODO 目前只给controller、service注解的类注入，日后扩展resource、component等等。controller等等都是component的子类
        if (!(clazz.isAnnotationPresent(MyController.class) || clazz.isAnnotationPresent(MyService.class))) {
            return;
        }

        // getFields()：获得某个类的所有的公共（public）的字段，包括父类中的字段。
        // getDeclaredFields()：获得某个类的所有声明的字段，即包括public、private和protected，但是不包括父类的申明字段。
        // TODO 看一下源码此处是如何处理的
        for (Field field : clazz.getDeclaredFields()) {
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
                    autowiredBeanName = toLowerFirstCase(field.getType().getSimpleName());
                }

                try {
                    /**************************循环依赖思路1***************************/
//                    if (null == factoryBeanInstanceCache.get(autowiredBeanName)) {
//                        withoutDIInstanceCache.put(beanWrapper, field);
//                        // 如果容器中不存在当前属性的bean，则直接跳过
//                        continue;
//                    }
//                    field.set(instance, this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappedInstance());
//                    withoutDIInstanceCache.remove(beanWrapper, field);
                    /**************************循环依赖思路1***************************/

                    /**************************循环依赖思路2***************************/
                    field.set(instance, getBean(autowiredBeanName));
                    /**************************循环依赖思路2***************************/
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
     * @param factoryBeanName
     * @return
     */
    private Object instantiateBean(String factoryBeanName, MyBeanDefinition beanDefinition) {
        Object instance = null;
//        String beanName = beanDefinition.getFactoryBeanName();
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
                // 赋值的同时，解析并为各方法创建增强点和编织增强方法的映射
                advisedSupport.setTargetClass(clazz);
                advisedSupport.setTarget(instance);

                // 判断规则，要不要生成代理类，如果要就调用代理工厂生成代理对象覆盖原生对象，并且放入三级缓存。如果不要就不做任何处理，返回原生对象
                if (advisedSupport.pointCutMatch()) {
                    // Method threw 'java.lang.NullPointerException' exception. Cannot evaluate com.sun.proxy.$Proxy5.toString()
                    // 策略工厂
                    instance = proxyFactory.createAopProxy(advisedSupport).getProxy();
//                    instance = new MyJdkDynamicAopProxy(advisedSupport).getProxy();
                }
                /************************AOP结束***********************/

                factoryBeanObjectCache.put(className, instance);
                factoryBeanObjectCache.put(factoryBeanName, instance);
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
        return reader.getContextConfig();
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
