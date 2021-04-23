package com.my.spring.framework.aop.config;

import lombok.Data;

/**
 * @ClassName MyAopConfig
 * @Description 保存是切面的配置
 * @Author ykq
 * @Date 2020/5/27
 * @Version v1.0.0
 */
@Data
public class MyAopConfig {
    /** 切入规则 */
    private String pointCut;

    /** 切面类 */
    private String aspectClass;

    /** before方法 */
    private String aspectBefore;

    /** after方法 */
    private String aspectAfter;

    /** afterThrow方法 */
    private String aspectAfterThrow;

    /** afterThrowingName */
    private String aspectAfterThrowingName;
}
