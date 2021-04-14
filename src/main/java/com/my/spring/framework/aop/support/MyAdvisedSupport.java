package com.my.spring.framework.aop.support;

import com.my.spring.framework.aop.aspect.MyAdvice;
import com.my.spring.framework.aop.config.MyAopConfig;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName MyAdvisedSupport
 * @Description 缓存到所有符合条件的待增强方法，每一个待增强的方法又对应一个Map，分别是after、before和afterThrow。每个类有一个，用于赋值动态代理类。
 * @Author ykq
 * @Date 2020/5/27
 * @Version v1.0.0
 */
@Data
public class MyAdvisedSupport {

    /** 切面的配置 */
    private MyAopConfig aopConfig;

    /** 需要被编织类的实例 */
    private Object target;

    /** 需要被编织的类的类型类 */
    private Class targetClass;

    /** 被编织类符合的正则 */
    private Pattern pointCutClassPattern;


    private Map<Method, Map<String, MyAdvice>> methodCache;

    public MyAdvisedSupport(MyAopConfig aopConfig) {
        this.aopConfig = aopConfig;
    }

    public void setTargetClass(Class targetClazz) {
        this.targetClass = targetClazz;
        // 解析
        parse();
    }

    private void parse() {
        // 把Spring的Express变成java能识别的正则表达式  // TODO 如果是自定义参数，不能硬编码为-4，怎么办？
        String pointCut = aopConfig.getPointCut()
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\\\.\\*", ".*")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)");

        // 生成匹配class的正则
        // TODO 验证是否是public .* com.my.demo.service..*ServiceImpl
        String pointCutForClassRegex = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);
        // TODO 验证是否是com.my.demo.service..*ServiceImpl
        String classNameLike = pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf(" ") + 1);
        // TODO 为什么class+" "+classNameLike
        pointCutClassPattern = Pattern.compile("class " + classNameLike);

        // 享元的共享池
        methodCache = new HashMap<>();

        // TODO 保存专门匹配方法的正则，有什么用？
        Pattern pointCutPattern = Pattern.compile(pointCut);
        try {
            // 创建切面类的类型类
            Class aspectClass = Class.forName(this.aopConfig.getAspectClass());

            // 遍历获得类型类的所有方法
            Map<String, Method> aspectMethods = new HashMap<>();
            for (Method method : aspectClass.getMethods()) {
                aspectMethods.put(method.getName(), method);
            }

            // 遍历目标编织类的方法，为每个方法初始化一套待织入的映射
            for (Method method : this.targetClass.getMethods()) {
                // TODO 验证toString获得的是什么？
                String methodString = method.toString();
                if(methodString.contains("throws")){  // 如果method有throw异常，则截断异常
                    methodString = methodString.substring(0,methodString.lastIndexOf("throws")).trim();
                }

                // TODO 验证。作用是判断目标方法是否符合切面定义的切点
                Matcher matcher = pointCutPattern.matcher(methodString);
                if(matcher.matches()){
                    Map<String,MyAdvice> adviceMap = new HashMap<>();
                    // 如果配置文件有前置通知的方法名
                    if(!(null == aopConfig.getAspectBefore() || "".equals(aopConfig.getAspectBefore()))){
                        // TODO key=before可否替换为aopConfig.getAspectBefore()
                        adviceMap.put("before", new MyAdvice(aspectClass.newInstance(), aspectMethods.get(aopConfig.getAspectBefore())));
                    }
                    // 如果配置文件有后置通知的方法名
                    if(!(null == aopConfig.getAspectAfter() || "".equals(aopConfig.getAspectAfter()))){
                        adviceMap.put("after", new MyAdvice(aspectClass.newInstance(), aspectMethods.get(aopConfig.getAspectAfter())));
                    }
                    // 如果配置文件有异常通知的方法名
                    if(!(null == aopConfig.getAspectAfterThrow() || "".equals(aopConfig.getAspectAfterThrow()))){
                        MyAdvice advice = new MyAdvice(aspectClass.newInstance(), aspectMethods.get(aopConfig.getAspectAfterThrow()));
                        advice.setThrowName(aopConfig.getAspectAfterThrowingName());
                        adviceMap.put("afterThrow",advice);
                    }

                    //跟目标代理类的业务方法和Advices建立一对多个关联关系，以便在Porxy类中获得
                    methodCache.put(method,adviceMap);
                }
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }

    public void setTarget(Object target) {
        this.target = target;
    }

    /**
     * 功能描述： 判断当前类型类是否符合生成代理类的正则规则
     * @author ykq
     * @date 2020/5/27 20:24
     * @param
     * @return
     */
    public boolean pointCutMath() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }

    /**
     * 功能描述： 根据一个目标类的方法获取其通知
     * @author ykq
     * @date 2020/5/27 20:45
     * @param
     * @return
     */
    public Map<String, MyAdvice> getAdvices(Method method, Object o) throws NoSuchMethodException {
        Map<String, MyAdvice> adviceMap = methodCache.get(method);
        if (null == adviceMap) {
            Method method1 = targetClass.getMethod(method.getName(), method.getParameterTypes());
            // TODO 这一步有问题吧？上一步拿不到，这一步就能拿到了吗？
            adviceMap = methodCache.get(method1);
            this.methodCache.put(method1, adviceMap);
        }
        return adviceMap;
    }
}
