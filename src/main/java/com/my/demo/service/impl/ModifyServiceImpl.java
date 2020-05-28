package com.my.demo.service.impl;

import com.my.demo.service.ModifyService;
import com.my.spring.framework.annotation.MyService;

/**
 * 功能描述：
 *
 * @author: ykq
 * @date: 2020/5/27 0:08
 */
@MyService
public class ModifyServiceImpl implements ModifyService {
    @Override
    public String add(String name, String addr) {
        return "modifyService add, name=" + name + ", addr=" + addr;
    }

    @Override
    public String edit(Integer id, String name) {
        return "modifyService edit, name=" + name + ", id=" + id;
    }

    @Override
    public String remove(Integer id) {
        return "modifyService remove, id=" + id;
    }
}
