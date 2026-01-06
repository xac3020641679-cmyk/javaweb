package com.enterprise.catering.controller;

import com.enterprise.catering.dao.OrderDAO;
import com.enterprise.catering.dao.UserDAO;
import com.enterprise.catering.model.Order;
import com.enterprise.catering.model.OrderItem;
import com.enterprise.catering.model.User;
import com.enterprise.catering.util.SystemTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 配送模块接口：配送员控制台、订单详情及打印数据。
 */
@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    @Autowired
    private OrderDAO orderDAO;
    @Autowired
    private UserDAO userDAO;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @GetMapping("/orders")
    public ResponseEntity<?> listOrders(@RequestParam(required = false) String date) throws Exception {
        boolean canPrint = SystemTimeUtil.isDeliveryStartTimeReached();
        Date deliveryDate = parseDateOrToday(date);
        List<Order> orders = orderDAO.getOrdersByDate(deliveryDate);

        // 为每个订单添加工位信息（优先使用订单中保存的，如果没有则从用户表获取）
        List<Map<String, Object>> ordersWithWorkLocation = new ArrayList<>();
        for (Order order : orders) {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.getId());
            orderMap.put("userId", order.getUserId());
            orderMap.put("userName", order.getUserName());
            orderMap.put("phone", order.getPhone());
            orderMap.put("orderTime", order.getOrderTime());
            orderMap.put("mealDate", order.getMealDate());
            orderMap.put("totalPrice", order.getTotalPrice());
            orderMap.put("createdAt", order.getCreatedAt());
            
            // 优先使用订单中保存的工位信息，如果没有则从用户表获取
            String workLocation = order.getWorkLocation();
            if (workLocation == null || workLocation.isEmpty()) {
                User user = userDAO.findById(order.getUserId());
                if (user != null && user.getWorkstation() != null) {
                    workLocation = user.getWorkstation();
                }
            }
            orderMap.put("workLocation", workLocation);
            
            ordersWithWorkLocation.add(orderMap);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("canPrint", canPrint);
        resp.put("deliveryDate", sdf.format(deliveryDate));
        resp.put("orders", ordersWithWorkLocation);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getOrder(@PathVariable int id) throws Exception {
        Order order = orderDAO.getOrderById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        List<OrderItem> items = orderDAO.getOrderItemsByOrderId(id);
        
        // 创建包含工位信息的订单对象
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("id", order.getId());
        orderMap.put("userId", order.getUserId());
        orderMap.put("userName", order.getUserName());
        orderMap.put("phone", order.getPhone());
        orderMap.put("orderTime", order.getOrderTime());
        orderMap.put("mealDate", order.getMealDate());
        orderMap.put("totalPrice", order.getTotalPrice());
        orderMap.put("createdAt", order.getCreatedAt());
        
        // 优先使用订单中保存的工位信息，如果没有则从用户表获取
        String workLocation = order.getWorkLocation();
        if (workLocation == null || workLocation.isEmpty()) {
            User user = userDAO.findById(order.getUserId());
            if (user != null && user.getWorkstation() != null) {
                workLocation = user.getWorkstation();
            }
        }
        orderMap.put("workLocation", workLocation);
        
        Map<String, Object> resp = new HashMap<>();
        resp.put("order", orderMap);
        resp.put("orderItems", items);
        return ResponseEntity.ok(resp);
    }


    @GetMapping("/orders/{id}/print-data")
    public ResponseEntity<?> getPrintData(@PathVariable int id) throws Exception {
        if (!SystemTimeUtil.isDeliveryStartTimeReached()) {
            return ResponseEntity.badRequest().body("delivery_not_started");
        }
        Order order = orderDAO.getOrderById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        List<OrderItem> items = orderDAO.getOrderItemsByOrderId(id);
        Map<String, Object> resp = new HashMap<>();
        resp.put("order", order);
        resp.put("orderItems", items);
        return ResponseEntity.ok(resp);
    }

    private Date parseDateOrToday(String dateStr) throws ParseException {
        if (dateStr == null || dateStr.isEmpty()) {
            return new Date();
        }
        return sdf.parse(dateStr);
    }
}


