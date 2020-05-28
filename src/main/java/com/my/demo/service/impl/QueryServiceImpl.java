package com.my.demo.service.impl;

import com.my.demo.service.QueryService;
import com.my.spring.framework.annotation.MyService;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 功能描述：
 *
 * @author: ykq
 * @date: 2020/5/26 23:47
 */
@Slf4j
@MyService
public class QueryServiceImpl implements QueryService {

    @Override
    public String query(String name) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = simpleDateFormat.format(new Date());
        String json = "{name: \"" + name + "\", time:\"" + time + "\"}";
        log.info("这是在业务方法中打印的：" + json);
        return json;
    }
}
