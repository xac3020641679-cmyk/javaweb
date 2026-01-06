package com.enterprise.catering.util;

/**
 * 系统配置类。
 * 数据库配置请使用 Spring Boot 的 application.yml。
 */
public class Config {

    // 文件上传路径
    public static final String UPLOAD_PATH = "/uploads/";

    // 订餐截止时间 (默认为上午9:00)
    public static final String ORDER_DEADLINE = "09:00";

    // 配餐开始时间 (默认为上午11:30)
    public static final String DELIVERY_START_TIME = "11:30";
}


