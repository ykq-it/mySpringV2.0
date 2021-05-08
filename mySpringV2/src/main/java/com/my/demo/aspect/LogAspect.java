package com.my.demo.aspect;

import com.my.spring.framework.aop.aspect.MyJoinPoint;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 功能描述： 作为织入的类
 * @author ykq
 * @date 2020/5/27 19:14
 * @param 
 * @return 
 */
@Slf4j
public class LogAspect {
    /****************简易版AOP******************/
/*    //在调用一个方法之前，执行before方法
    public void before(){
        //这个方法中的逻辑，是由我们自己写的
//        System.out.println("Invoker Before Method!!!");
        log.info("Invoker Before Method!!!");
    }
    //在调用一个方法之后，执行after方法
    public void after(){
        System.out.println("Invoker After Method!!!");
        log.info("Invoker After Method!!!");
    }

    public void afterThrowing(){
        System.out.println("throw Exception!!!");
        log.info("throw Exception");
    }*/

    /****************可传参版AOP******************/
    public void before(MyJoinPoint joinPoint){
        System.out.println("Invoker Before Method!!!");
        log.info("Invoker Before Method!!!");
        joinPoint.setUserAttribute("startTime_" + joinPoint.getMethod().getName(), System.currentTimeMillis());

    }

    public void after(MyJoinPoint joinPoint, Object returnValue){
        System.out.println("Invoker After Method!!!");
        log.info("Invoker After Method!!!");
        long startTime = (long) joinPoint.getUserAttribute("startTime_" + joinPoint.getMethod().getName());
        long endTime = System.currentTimeMillis();
        System.out.println("Invoker After Method!!!   use time: " + (endTime - startTime) + ". return: " + returnValue);
        log.info("Invoker After Method!!!   use time: " + (endTime - startTime) + ". return: " + returnValue);
    }

    public void afterThrowing(MyJoinPoint joinPoint, Throwable ex, Object returnValue){
        System.out.println("throw Exception!!!" + ex.getMessage());
        log.info("throw Exception" + ex.getMessage());
    }
}
