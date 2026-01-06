package com.enterprise.catering.controller;

import com.enterprise.catering.dao.BlanketOrderDAO;
import com.enterprise.catering.model.BlanketOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 厨房相关接口：总括订单查看与生成。
 */
@RestController
@RequestMapping("/api/kitchen")
public class KitchenController {

    @Autowired
    private BlanketOrderDAO blanketOrderDAO;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @GetMapping("/blanket-orders")
    public ResponseEntity<?> getBlanketOrders(@RequestParam(required = false) String date) throws ParseException {
        Date reportDate = parseDateOrToday(date);
        List<BlanketOrder> blanketOrders = blanketOrderDAO.getBlanketOrdersByDate(reportDate);
        double totalAmount = blanketOrderDAO.calculateTotalAmount(reportDate);

        Map<String, Object> resp = new HashMap<>();
        resp.put("blanketOrders", blanketOrders);
        resp.put("totalAmount", totalAmount);
        resp.put("reportDate", sdf.format(reportDate));
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/blanket-orders/generate")
    public ResponseEntity<?> generateBlanketOrder(@RequestParam(required = false) String date) throws ParseException {
        Date reportDate = parseDateOrToday(date);
        boolean success = blanketOrderDAO.generateBlanketOrder(reportDate);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", success);
        resp.put("reportDate", sdf.format(reportDate));
        resp.put("blanketOrders", blanketOrderDAO.getBlanketOrdersByDate(reportDate));
        resp.put("totalAmount", blanketOrderDAO.calculateTotalAmount(reportDate));
        return ResponseEntity.ok(resp);
    }

    private Date parseDateOrToday(String dateStr) throws ParseException {
        if (dateStr == null || dateStr.isEmpty()) {
            return new Date();
        }
        return sdf.parse(dateStr);
    }
}


