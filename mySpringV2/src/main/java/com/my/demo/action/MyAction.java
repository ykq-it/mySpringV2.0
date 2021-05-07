package com.my.demo.action;

import com.my.demo.service.ModifyService;
import com.my.demo.service.QueryService;
import com.my.spring.framework.annotation.MyAutowired;
import com.my.spring.framework.annotation.MyController;
import com.my.spring.framework.annotation.MyRequestMapping;
import com.my.spring.framework.annotation.MyRequestParam;
import com.my.spring.framework.webmvc.MyModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 功能描述：
 *
 * @author: ykq
 * @date: 2020/5/27 0:11
 */
@MyController
@MyRequestMapping("/web")
public class MyAction {
    @MyAutowired
    ModifyService modifyService;

    @MyAutowired
    QueryService queryService;

    @MyRequestMapping("/query.json")
    public MyModelAndView query(HttpServletRequest request, HttpServletResponse response, @MyRequestParam("name") String name) {
        String result = queryService.query(name);
        return out(response, result);
    }

    @MyRequestMapping("/add*.json")
    public MyModelAndView add(HttpServletRequest request, HttpServletResponse response,
                              @MyRequestParam("name") String name, @MyRequestParam("addr") String addr){
        String result = modifyService.add(name,addr);
        return out(response,result);
    }

    @MyRequestMapping("/remove.json")
    public MyModelAndView remove(HttpServletRequest request, HttpServletResponse response,
                                 @MyRequestParam("id") Integer id){
        String result = modifyService.remove(id);
        return out(response,result);
    }

    @MyRequestMapping("/edit.json")
    public MyModelAndView edit(HttpServletRequest request,HttpServletResponse response,
                               @MyRequestParam("id") Integer id,
                               @MyRequestParam("name") String name){
        String result = modifyService.edit(id,name);
        return out(response,result);
    }

    private MyModelAndView out(HttpServletResponse response, String result) {
        try {
            response.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
