package com.my.demo.action;

import com.my.demo.service.DemoService;
import com.my.spring.framework.annotation.MyAutowired;
import com.my.spring.framework.annotation.MyController;
import com.my.spring.framework.annotation.MyRequestMapping;
import com.my.spring.framework.annotation.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @ClassName DemoController
 * @Description TODO
 * @Author ykq
 * @Date 2020/4/29
 * @Version v1.0.0
 */
@MyController
@MyRequestMapping("/demo")
public class DemoAction {

    @MyAutowired
    public DemoService demoService;

    @MyRequestMapping("/query")
    public void query(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @MyRequestParam("name") String name) {
        String result = demoService.get(name);

        try {
            httpServletResponse.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
