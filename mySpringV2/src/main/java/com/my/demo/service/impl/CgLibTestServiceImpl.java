package com.my.demo.service.impl;

import com.my.spring.framework.annotation.MyService;

/**
 * @ClassName CgLibTestService
 * @Description TODO
 * @Author ykq
 * @Date 2021/05/08
 * @Version v1.0.0
 */
@MyService
public class CgLibTestServiceImpl {

    public String add(String name, String addr) {
        System.out.println("doBusiness!!");
        return "CgLibTestService add, name=" + name + ", addr=" + addr;
    }
}
