package com.my.spring.framework.webmvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName MyView
 * @Description TODO
 * @Author ykq
 * @Date 2020/5/19
 * @Version v1.0.0
 */
public class MyView {
    /** 设置默认内容类型 */
    public static  final String DEFAULT_CONTENT_TYPE = "text/html;charset=utf-8";

    private File viewFile;

    public MyView(File viewFile) {
        this.viewFile = viewFile;
    }

    public static String getDefaultContentType() {
        return DEFAULT_CONTENT_TYPE;
    }

    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        StringBuffer sb = new StringBuffer();
        // 随机访问文件流，mode参数指定用以打开文件的访问模式。
        /*
        "r"	以只读方式打开。调用结果对象的任何 write 方法都将导致抛出 IOException。
        "rw"	打开以便读取和写入。如果该文件尚不存在，则尝试创建该文件。
        "rws"	打开以便读取和写入，对于 "rw"，还要求对文件的内容或元数据的每个更新都同步写入到底层存储设备。
        "rwd"  	打开以便读取和写入，对于 "rw"，还要求对文件内容的每个更新都同步写入到底层存储设备。*/
        RandomAccessFile ra = new RandomAccessFile(this.viewFile, "r");

        try {
            String line = null;
            // 如果读取的流下一行不为空
            while (null != (line = ra.readLine())) {
                // 转换此行的编码格式
                line = new String(line.getBytes("ISO-8859-1"), "utf-8");
                // 将给定的正则表达式编译到具有给定标志的模式中。 CASE_INSENSITIVE启用不区分大小写的匹配。
                // TODO 正则表达式对不对
                // Pattern指定为字符串的正则表达式必须首先被编译为此类的实例。然后，可将得到的模式用于创建 Matcher 对象，依照正则表达式，该对象可以与任意字符序列匹配。执行匹配所涉及的所有状态都驻留在匹配器中
                Pattern pattern = Pattern.compile("$\\{[^\\}]+\\}", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(line);

                while (matcher.find()) {
                    String paramName = matcher.group();
                    paramName = paramName.replaceAll("$\\{|\\}", "");
                    Object paramValue = model.get(paramName);
                    if (null == paramValue) {
                        continue;
                    }
                    // 要把${}中间的字符取出来
                    line = matcher.replaceFirst(makeStringForRegExp(paramValue.toString()));
                    matcher = pattern.matcher(line);
                }
                sb.append(line);
            }

        } finally {
            ra.close();
        }

        response.setCharacterEncoding("utf-8");
        response.setContentType(DEFAULT_CONTENT_TYPE);
        response.getWriter().write(sb.toString());
    }

    // 处理特殊字符
    private String makeStringForRegExp(String string) {
        return string.replace("\\", "\\\\").replace("*", "\\*")
                .replace("+", "\\+").replace("|", "\\|")
                .replace("{", "\\{").replace("}", "\\}")
                .replace("(", "\\(").replace(")", "\\)")
                .replace("^", "\\^").replace("$", "\\$")
                .replace("[", "\\[").replace("]", "\\]")
                .replace("?", "\\?").replace(",", "\\,")
                .replace(".", "\\.").replace("&", "\\&");
    }
}
