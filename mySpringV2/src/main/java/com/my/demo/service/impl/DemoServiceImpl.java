package com.my.demo.service.impl;

import com.my.demo.action.DemoAction;
import com.my.demo.service.DemoService;
import com.my.spring.framework.annotation.MyAutowired;
import com.my.spring.framework.annotation.MyService;

/**
 * @ClassName DemoServiceImpl
 * @Description TODO
 * @Author ykq
 * @Date 2020/4/29
 * @Version v1.0.0
 */
@MyService
public class DemoServiceImpl implements DemoService {
    @MyAutowired
    private DemoAction demoAction;

    @Override
    public String get(String name) {
        return "My name is " + name;
    }
}
