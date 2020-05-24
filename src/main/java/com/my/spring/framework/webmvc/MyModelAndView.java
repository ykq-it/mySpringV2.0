package com.my.spring.framework.webmvc;

import java.util.Map;

/**
 * @ClassName MyModelAndView
 * @Description TODO
 * @Author ykq
 * @Date 2020/5/18
 * @Version v1.0.0
 */
public class MyModelAndView {
    /** 页面模板的名称 */
    private String viewName;

    /** 缓存返回值，往页面传送的参数 */
    private Map<String, ?> model;

    public MyModelAndView(String viewName) {
        this(viewName, null);
    }

    public MyModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }

    public void setModel(Map<String, ?> model) {
        this.model = model;
    }
}
