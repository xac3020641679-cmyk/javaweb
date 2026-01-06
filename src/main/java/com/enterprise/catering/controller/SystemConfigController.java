package com.enterprise.catering.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置接口
 */
@RestController
@RequestMapping("/api/config")
public class SystemConfigController {

    private static final Map<String, String> systemConfig = new HashMap<>();

    static {
        systemConfig.put("order_deadline", "09:00");
        systemConfig.put("delivery_start_time", "11:30");
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> getConfig() {
        return ResponseEntity.ok(systemConfig);
    }

    @PostMapping
    public ResponseEntity<?> update(@RequestBody Map<String, String> body) {
        String orderDeadline = body.get("order_deadline");
        String deliveryStartTime = body.get("delivery_start_time");

        if (orderDeadline != null && !orderDeadline.isEmpty()) {
            systemConfig.put("order_deadline", orderDeadline);
        }
        if (deliveryStartTime != null && !deliveryStartTime.isEmpty()) {
            systemConfig.put("delivery_start_time", deliveryStartTime);
        }
        return ResponseEntity.ok(systemConfig);
    }

    public static String getOrderDeadline() {
        return systemConfig.getOrDefault("order_deadline", "09:00");
    }

    public static String getDeliveryStartTime() {
        return systemConfig.getOrDefault("delivery_start_time", "11:30");
    }
}


