package com.my.spring.framework.beans.support;

import com.my.spring.framework.beans.config.MyBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @ClassName （配置封装模块-支持模块）MyBeanDefinitionReader
 * @Description 对application.properties配置文件的解析，实现逻辑非常简单。通过构造方法获取从ApplicationContext传过来的locations配置文件路径，然后解析，扫描并保存所有相关的类，并提供统一的访问接口
 * @Author ykq
 * @Date 2020/5/13
 * @Version v1.0.0
 */
public class MyBeanDefinitionReader {
    // 从配置文件进行查找、读取、解析

    /** 保存扫描到的所有需要注入的类的全路径 */
    private List<String> registerBeanClasses = new ArrayList<>();

    /** 保存配置文件信息 */
    private Properties contextConfig = new Properties();

    /** 固定配置文件中的key，相当于XML的规范？？ */
    private final String SCAN_PACKAGE = "scanPackage";

    // TODO 这个方法有用吗？答：在初始化视图解析器时需要获得配置文件中模板的路径
    public Properties getContextConfig() {
        return this.contextConfig;
    }

    /**
     * 功能描述：处理web.xml中的配置文件并转为Properties配置类。接着触发包扫描
     * @author ykq
     * @date 2020/5/13 20:28
     * @param
     * @return
     */
    public MyBeanDefinitionReader(String... locations) {
        // 加载配置
        doLoadConfig(locations[0]);

        // 配置保存完毕，开始扫描包
        doScanner(contextConfig.getProperty(SCAN_PACKAGE));
    }

    private void doLoadConfig(String location) {
        // 通过URL定位找到其所对应的文件，然后转换为文件流
        // getClassLoader()获取的可以理解为/WEB-INF/classes/的目录
        // TODO 此处仅加载了一个配置文件，多配置文件情况下需要拓展
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(location.replace("classpath:", ""));

        try {
            // 将配置文件中的信息加载到Properties里
            // TODO 如果多配置文件，一样，遍历is加载到Properties中
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 功能描述： 扫描（遍历）配置文件指定的包，保存全类名到reader的registerBeanClasses。以后需要注册到IoC的类的，以便反射生成实例
     * @author ykq
     * @date 2020/5/13 20:37
     * @param
     * @return
     */
    private void doScanner(String scanPackage) {
        // 将scanPackage的类路径转换为（根目录下的）文件路径（.替换为/），为了遍历文件。
        // 得到的url为/D:/Tools/apache-tomcat-7.0.59/webapps/mySpring2_war/WEB-INF/classes/com/my/demo/
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replace(".", "/"));
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                // 过滤掉非class文件
                if (!file.getName().endsWith(".class")) {
                    continue;
                }

                // 获得class文件的全路径，并保存的全局变量
                String className = scanPackage + "." + file.getName().replace(".class", "");
                this.registerBeanClasses.add(className);
            }
        }
    }

    /**
     * 功能描述： 遍历待注册的全类名，为每个类生成定义类
     * 保存所有要注册到IoC的类的定义（beanName、全类名className、是否懒加载lazyInit），以便之后的IoC操作
     * @author ykq
     * @date 2020/5/13 21:19
     * @param
     * @return
     */
    public List<MyBeanDefinition> loadBeanDefinitions() {
        List<MyBeanDefinition> result = new ArrayList<>();

        for (String className: registerBeanClasses) {
            try {
                // 获得className对应的类型类
                Class beanClass = Class.forName(className);

                // 过滤掉接口，因为后面要拿MyBeanDefinition的className new对象，但接口不能创建对象。
                if (beanClass.isInterface()) {
                    continue;
                }

                // 将beanDefinition保存到List中，getSimpleName()获取类名，getName()获取全类名
                // TODO 1、自定义factoryBeanName 2、可以用className替换beanClass.getName()
                result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()), beanClass.getName()));

                // 找到当前类型类所实现的接口，并将类型类的对象赋值给接口
                // 一个实现类对应多个接口时怎么处理？只能覆盖，因为Spring就是这么傻
                Class<?>[] interfaces = beanClass.getInterfaces();
                for (Class iClass : interfaces) {
                    result.add(doCreateBeanDefinition(toLowerFirstCase(iClass.getSimpleName()), beanClass.getName()));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 功能描述： 创建Bean定义的对象
     * @author ykq
     * @date 2020/5/13 21:34
     * @param
     * @return
     */
    private MyBeanDefinition doCreateBeanDefinition(String factoryBeanName, String className) {
        // 封装Bean定义对象
        MyBeanDefinition beanDefinition = new MyBeanDefinition();
        beanDefinition.setFactoryBeanName(factoryBeanName);
        beanDefinition.setBeanClassName(className);
        return beanDefinition;
    }

    /**
     * 功能描述： 获取首字母小写的beanName
     * @author ykq
     * @date 2020/5/13 21:29
     * @param
     * @return
     */
    private String toLowerFirstCase(String simpleName) {
        char[] c = simpleName.toCharArray();
        c[0] += 32;
//        不能return c.toString();
        return String.valueOf(c);
    }
}
