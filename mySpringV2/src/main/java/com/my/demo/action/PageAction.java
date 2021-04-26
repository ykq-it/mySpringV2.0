//package com.my.demo.action;
//
//import com.my.spring.framework.annotation.MyController;
//import com.my.spring.framework.annotation.MyRequestMapping;
//import com.my.spring.framework.annotation.MyRequestParam;
//import com.my.spring.framework.webmvc.MyModelAndView;
//
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * 功能描述：
// *
// * @author: ykq
// * @date: 2020/5/23 20:03
// */
//@MyController
//@MyRequestMapping("/page")
//public class PageAction {
//
//    @MyRequestMapping("/first.html")
//    public MyModelAndView query(@MyRequestParam("name") String name) {
//        Map<String, Object> model = new HashMap<>();
//        model.put("name", name);
//        model.put("date", new Date());
//        model.put("token", "123456");
//        return new MyModelAndView("first.html", model);
//    }
//}
