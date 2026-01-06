package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 启动类：
 * 统一对外提供 /api/** 的 REST 接口，供 Vue 前端调用。
 */

@SpringBootApplication(scanBasePackages = {"org.example", "com.enterprise.catering"})
public class QyctSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(QyctSystemApplication.class, args);
    }
}


