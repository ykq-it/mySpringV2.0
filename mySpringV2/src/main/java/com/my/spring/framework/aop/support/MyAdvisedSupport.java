package com.my.spring.framework.aop.support;

import com.my.spring.framework.aop.aspect.MyAdvice;
import com.my.spring.framework.aop.aspect.MyAfterReturningAdviceInterceptor;
import com.my.spring.framework.aop.aspect.MyAspectJAfterThrowingAdvice;
import com.my.spring.framework.aop.aspect.MyMethodBeforeAdviceInterceptor;
import com.my.spring.framework.aop.config.MyAopConfig;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

    /******缓存method与增强关系的硬编码*****/
//    private Map<Method, Map<String, MyAdvice>> methodCache;
    /******缓存method与增强关系的硬编码*****/
    private Map<Method, List<Object>> methodCache;


    public MyAdvisedSupport(MyAopConfig aopConfig) {
        this.aopConfig = aopConfig;
    }

    public void setTargetClass(Class targetClazz) {
        this.targetClass = targetClazz;
        // 解析
        parse();
    }

    private void parse() {
        // 把Spring的Express变成java能识别的正则表达式  // TODO 如果是自定义方法名，如形参以name开头(.name*)，不能硬编码为-4，怎么办？
        // 权限修饰符 返回值类型 包名.类名.方法名(形参列表)
        // public .* com.my.demo.service..*ServiceImpl..*(.*)
        // 得到真正的正则表达式
        String pointCutRegex = aopConfig.getPointCut()
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\\\.\\*", ".*")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)");

        // 生成匹配class的正则
        // 验证是public .* com.my.demo.service..*ServiceImpl
        String pointCutForClassRegex = pointCutRegex.substring(0, pointCutRegex.lastIndexOf("\\(") - 4);
        // 验证是com.my.demo.service..*ServiceImpl
        String classNameLike = pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf(" ") + 1);
        // 为什么class+" "+classNameLike？因为正则匹配的this.targetClass.toString()是这个格式
        pointCutClassPattern = Pattern.compile("class " + classNameLike);

        // 享元的共享池，保存回调通知和目标切点之间的关系。如：
        // query方法要织入 before() after()
        // add方法要织入 afterThrowing()  rollback()
        methodCache = new HashMap<>();

        // 编译并保存专门匹配方法的正则，有什么用？
        Pattern pointCutPattern = Pattern.compile(pointCutRegex);
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
                // 验证toString获得的是什么？ public java.lang.String com.my.demo.service.impl.DemoServiceImpl.get(java.lang.String) 生成的字符串的规则和切面配置相同，用于正则匹配
                String methodString = method.toString();
                if(methodString.contains("throws")){
                    // 如果method有throw异常，则截掉异常
                    methodString = methodString.substring(0,methodString.lastIndexOf("throws")).trim();
                }

                // 作用是判断目标方法是否符合切面定义的切点
                Matcher matcher = pointCutPattern.matcher(methodString);
                if(matcher.matches()){
//                    Map<String,MyAdvice> adviceMap = new HashMap<>();
//                    // 如果配置文件有前置通知的方法名
//                    if(null != aopConfig.getAspectBefore() && !"".equals(aopConfig.getAspectBefore())){
//                        // key=before可否替换为aopConfig.getAspectBefore()？可以，但没必要，这里的字符串便是对配置文件key的规定
//                        adviceMap.put("before", new MyAdvice(aspectClass.newInstance(), aspectMethods.get(aopConfig.getAspectBefore())));
//                    }
//                    // 如果配置文件有后置通知的方法名
//                    if(null != aopConfig.getAspectAfter() && !"".equals(aopConfig.getAspectAfter())){
//                        adviceMap.put("after", new MyAdvice(aspectClass.newInstance(), aspectMethods.get(aopConfig.getAspectAfter())));
//                    }
//                    // 如果配置文件有异常通知的方法名
//                    if(null != aopConfig.getAspectAfterThrow() && !"".equals(aopConfig.getAspectAfterThrow())){
//                        MyAdvice advice = new MyAdvice(aspectClass.newInstance(), aspectMethods.get(aopConfig.getAspectAfterThrow()));
//                        advice.setThrowName(aopConfig.getAspectAfterThrowingName());
//                        adviceMap.put("afterThrow",advice);
//                    }
//                    //跟目标代理类的业务方法和Advices建立一对多个关联关系，以便在Porxy类中获得
//                    methodCache.put(method,adviceMap);

                    List<Object> advices = new LinkedList<>();
                    // TODO 这里可以改成循环，用策略模式，可以实现动态扩展
                    // 如果配置文件有前置通知的方法名，创建对应的拦截器，入参切入的实例，和切入的方法
                    if(null != aopConfig.getAspectBefore() && !"".equals(aopConfig.getAspectBefore())){
                        advices.add(new MyMethodBeforeAdviceInterceptor(aspectClass.newInstance(), aspectMethods.get(aopConfig.getAspectBefore())));
                    }
                    if(null != aopConfig.getAspectAfter() && !"".equals(aopConfig.getAspectAfter())){
                        advices.add(new MyAfterReturningAdviceInterceptor(aspectClass.newInstance(), aspectMethods.get(aopConfig.getAspectAfter())));
                    }
                    if(null != aopConfig.getAspectAfterThrow() && !"".equals(aopConfig.getAspectAfterThrow())){
                        MyAspectJAfterThrowingAdvice advice = new MyAspectJAfterThrowingAdvice(aspectClass.newInstance(), aspectMethods.get(aopConfig.getAspectAfterThrow()));
                        advice.setThrowName(aopConfig.getAspectAfterThrowingName());
                        advices.add(advice);
                    }
                    methodCache.put(method,advices);
                }
            }

        } catch (Exception e) {
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
    public boolean pointCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }

    /******缓存method与增强关系的硬编码*****/
    /**
     * 功能描述： 根据一个目标类的方法获取其通知
     * @author ykq
     * @date 2020/5/27 20:45
     * @param
     * @return
     */
//    public Map<String, MyAdvice> getAdvices(Method method) throws NoSuchMethodException {
//        Map<String, MyAdvice> adviceMap = methodCache.get(method);
//        if (null == adviceMap) {
//            // 因为传入的method方法可能是代理对象的method，虽然名字相同，但不是一个同对象。所以重新利用相同的名字和形参拿到原始的Method对象
//            Method method1 = targetClass.getMethod(method.getName(), method.getParameterTypes());
//            adviceMap = methodCache.get(method1);
//            methodCache.put(method1, adviceMap);
//        }
//        return adviceMap;
//    }
    /******缓存method与增强关系的硬编码*****/

    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) throws Exception {
        List<Object> adviceMap = methodCache.get(method);
        if (null == adviceMap) {
            // 因为传入的method方法可能是代理对象的method，虽然名字相同，但不是一个同对象。所以重新利用相同的名字和形参拿到原始的Method对象
            Method method1 = targetClass.getMethod(method.getName(), method.getParameterTypes());
            adviceMap = methodCache.get(method1);
            methodCache.put(method1, adviceMap);
        }
        return adviceMap;
    }
}
