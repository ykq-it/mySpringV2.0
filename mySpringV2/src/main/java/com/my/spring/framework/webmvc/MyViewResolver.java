package com.my.spring.framework.webmvc;

import java.io.File;
import java.util.Locale;

/**
 * @ClassName MyViewResolver 也算适配器模式，根据不同的请求选择不同的模板引擎来进行页面的渲染
 * @Description 设计这个类的主要目的是： 将具体的页面变成一个View对象
 * 1、将一个静态文件变成一个动态文件
 * 2、根据用户传入不同的参数，产生不同的结果
 * 最终输出字符串，交给Response输出
 * @Author ykq
 * @Date 2020/5/18
 * @Version v1.0.0
 */
public class MyViewResolver {

    // TODO 可扩展不同的模板引擎，.vm,.ftl,.jsp等
    private final String DEFAULT_TEMPLATE_SUFFIX = ".html";

    private File templateRootDir;

    private String viewName;

    public MyViewResolver(File template) {
        this.templateRootDir = template;
        this.viewName = template.getName();
    }

    /***
     * 功能描述: 入参模板路径--layout
     * @author ykq
     * @date 2020/5/24 14:14
     * @param
     * @return
     */
    /*public MyViewResolver(String templateRoot) {
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        this.templateRootDir = new File(templateRootPath);
    }*/

    public String getViewName() {
        return viewName;
    }

    public MyView resolveViewName(String viewName, Locale locale) {
//        this.viewName = viewName;
        if (null == viewName || "".equals(viewName.trim())) {
            return null;
        }
//        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX) ? viewName : (viewName + DEFAULT_TEMPLATE_SUFFIX);
//        File templateFile = new File((templateRootDir.getPath() + "/" + viewName).replaceAll("/+", "/"));
//        return new MyView(templateFile);
        return new MyView(templateRootDir);
    }

    public String packAimViewName(String aimViewName) {
        return aimViewName.endsWith(DEFAULT_TEMPLATE_SUFFIX) ? aimViewName : (aimViewName + DEFAULT_TEMPLATE_SUFFIX);
    }
}
