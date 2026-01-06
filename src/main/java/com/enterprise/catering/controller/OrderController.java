package com.enterprise.catering.controller;

import com.enterprise.catering.dao.MenuItemDAO;
import com.enterprise.catering.dao.OrderDAO;
import com.enterprise.catering.dao.UserDAO;
import com.enterprise.catering.model.MenuItem;
import com.enterprise.catering.model.Order;
import com.enterprise.catering.model.OrderItem;
import com.enterprise.catering.model.User;
import com.enterprise.catering.util.SystemTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单相关 REST 接口：
 * - GET  /api/orders        : 当前用户或全部订单列表
 * - GET  /api/orders/{id}   : 订单详情（含订单项）
 * - POST /api/orders        : 创建订单（单菜品版，对应原 newOrder.jsp 表单）
 *
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderDAO orderDAO;
    @Autowired
    private MenuItemDAO menuItemDAO;
    @Autowired
    private UserDAO userDAO;

    @GetMapping
    public ResponseEntity<?> listOrders(@RequestParam(required = false) Integer userId,
                                        @RequestParam(required = false, defaultValue = "employee") String role)
            {
        List<Order> orders;
        if ("employee".equals(role) && userId != null) {
            orders = orderDAO.getOrdersByUserId(userId);
        } else {
            orders = orderDAO.getAllOrders();
        }
        return ResponseEntity.ok(orders);
    }

    /**
     * 订餐可用性查询：
     * - 返回当前“可订餐日期”（今天或明天，取决于配餐开始时间）
     * - 返回该用户当天是否已下单
     * - 返回当天是否已超过订餐截止时间
     * - 返回当前截止时间和配餐开始时间配置，便于前端展示
     */
    @GetMapping("/availability")
    public ResponseEntity<?> availability(@RequestParam int userId) {
        Date orderDate = SystemTimeUtil.getAvailableOrderDate();
        boolean deadlinePassed = SystemTimeUtil.isOrderDeadlinePassedForDate(orderDate);
        boolean hasOrdered = orderDAO.hasOrderByUserIdAndDate(userId, orderDate);

        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");

        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("orderDate", df.format(orderDate));
        resp.put("deadlinePassed", deadlinePassed);
        resp.put("hasOrderedToday", hasOrdered);
        resp.put("orderDeadline", com.enterprise.catering.controller.SystemConfigController.getOrderDeadline());
        resp.put("deliveryStartTime", com.enterprise.catering.controller.SystemConfigController.getDeliveryStartTime());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable int id) {
        Order order = orderDAO.getOrderById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        List<OrderItem> items = orderDAO.getOrderItemsByOrderId(id);
        return ResponseEntity.ok(new OrderDetailResponse(order, items));
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest req) {
        if (req.getUserId() == null) {
            return ResponseEntity.badRequest().body("missing_params");
        }

        // 支持两种格式：单个菜品（向后兼容）和多个菜品
        List<OrderItemRequest> items = req.getItems();
        if (items == null || items.isEmpty()) {
            // 向后兼容：如果 items 为空，尝试使用旧的 menuItemId 和 quantity
            if (req.getMenuItemId() != null && req.getQuantity() != null) {
                items = new ArrayList<>();
                OrderItemRequest singleItem = new OrderItemRequest();
                singleItem.setMenuItemId(req.getMenuItemId());
                singleItem.setQuantity(req.getQuantity());
                items.add(singleItem);
            } else {
                return ResponseEntity.badRequest().body("missing_params");
            }
        }

        Date orderDate = SystemTimeUtil.getAvailableOrderDate();

        if (SystemTimeUtil.isOrderDeadlinePassedForDate(orderDate)) {
            return ResponseEntity.badRequest().body("deadline_passed");
        }

        if (orderDAO.hasOrderByUserIdAndDate(req.getUserId(), orderDate)) {
            return ResponseEntity.badRequest().body("already_ordered_today");
        }

        // 获取用户的工位信息
        User user = userDAO.findById(req.getUserId());
        String workLocation = null;
        if (user != null) {
            workLocation = user.getWorkstation();
        }

        // 验证所有菜品并计算总价
        double totalPrice = 0;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (OrderItemRequest itemReq : items) {
            MenuItem menuItem = menuItemDAO.getMenuItemById(itemReq.getMenuItemId());
            if (menuItem == null) {
                return ResponseEntity.badRequest().body("invalid_menu_item: " + itemReq.getMenuItemId());
            }
            
            int quantity = itemReq.getQuantity();
            if (quantity < 1) {
                return ResponseEntity.badRequest().body("invalid_quantity");
            }
            
            double subtotal = menuItem.getPrice() * quantity;
            totalPrice += subtotal;
            
            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItemId(menuItem.getId());
            orderItem.setName(menuItem.getName());
            orderItem.setUnit(menuItem.getUnit());
            orderItem.setQuantity(quantity);
            orderItem.setPrice(menuItem.getPrice());
            orderItem.setSubtotal(subtotal);
            orderItems.add(orderItem);
        }

        // 创建订单
        Order order = new Order();
        order.setUserId(req.getUserId());
        order.setUserName(req.getUserName());
        order.setPhone(req.getPhone());
        order.setWorkLocation(workLocation);
        order.setMealDate(orderDate);
        order.setTotalPrice(totalPrice);

        orderDAO.addOrder(order);

        // 添加所有订单项
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
            orderDAO.addOrderItem(orderItem);
        }

        return ResponseEntity.ok(order);
    }

    // DTO
    public static class CreateOrderRequest {
        private Integer userId;
        private String userName;
        private String phone;
        // 向后兼容：单个菜品
        private Integer menuItemId;
        private Integer quantity;
        // 新格式：多个菜品
        private List<OrderItemRequest> items;

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        // 向后兼容
        public Integer getMenuItemId() {
            return menuItemId;
        }

        public void setMenuItemId(Integer menuItemId) {
            this.menuItemId = menuItemId;
        }

        // 向后兼容
        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        // 新格式：多个菜品
        public List<OrderItemRequest> getItems() {
            return items;
        }

        public void setItems(List<OrderItemRequest> items) {
            this.items = items;
        }
    }

    public static class OrderItemRequest {
        private Integer menuItemId;
        private Integer quantity;

        public Integer getMenuItemId() {
            return menuItemId;
        }

        public void setMenuItemId(Integer menuItemId) {
            this.menuItemId = menuItemId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

    public static class OrderDetailResponse {
        private Order order;
        private List<OrderItem> items;

        public OrderDetailResponse(Order order, List<OrderItem> items) {
            this.order = order;
            this.items = items;
        }

        public Order getOrder() {
            return order;
        }

        public void setOrder(Order order) {
            this.order = order;
        }

        public List<OrderItem> getItems() {
            return items;
        }

        public void setItems(List<OrderItem> items) {
            this.items = items;
        }
    }
}


