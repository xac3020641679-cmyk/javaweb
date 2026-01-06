package com.enterprise.catering.controller;

import com.enterprise.catering.dao.OrderDAO;
import com.enterprise.catering.dao.UserDAO;
import com.enterprise.catering.model.Order;
import com.enterprise.catering.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 财务报表相关接口，对应原 SalesReportServlet 的主要功能。
 */
@RestController
@RequestMapping("/api/reports")
public class FinanceReportController {

    @Autowired
    private OrderDAO orderDAO;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 月度销售统计（按菜品汇总）
     * GET /api/reports/monthly-sales?month=yyyy-MM
     */
    @GetMapping("/monthly-sales")
    public ResponseEntity<?> monthlySales(@RequestParam(required = false) String month) {
        Date reportDate = parseMonthOrDefault(month);
        
        List<SalesItem> salesData = generateMonthlySalesData(reportDate);
        double totalAmount = calculateMonthlyTotal(salesData);

        Map<String, Object> resp = new HashMap<>();
        resp.put("salesData", salesData);
        resp.put("totalAmount", totalAmount);
        resp.put("reportMonth", new SimpleDateFormat("yyyy-MM").format(reportDate));
        return ResponseEntity.ok(resp);
    }

    /**
     * 员工月度订单汇总（经理/财务查看某员工）
     * GET /api/reports/employee-summary?userId=&month=yyyy-MM
     */
    @GetMapping("/employee-summary")
    public ResponseEntity<?> employeeSummary(@RequestParam int userId,
                                             @RequestParam(required = false) String month) {
        Date reportDate = parseMonthOrDefault(month);
        User employee = userDAO.findById(userId);
        if (employee == null) {
            return ResponseEntity.notFound().build();
        }
        List<SalesItem> salesData = generateEmployeeSalesData(userId, reportDate);
        double totalAmount = calculateMonthlyTotal(salesData);
        List<Order> monthlyOrders = orderDAO.getOrdersByUserIdAndMonth(userId, reportDate);

        Map<String, Object> resp = new HashMap<>();
        resp.put("employee", employee);
        resp.put("salesData", salesData);
        resp.put("totalAmount", totalAmount);
        resp.put("reportMonth", new SimpleDateFormat("yyyy-MM").format(reportDate));
        resp.put("monthlyOrders", monthlyOrders);
        return ResponseEntity.ok(resp);
    }

    /**
     * 个人月度订单统计（员工自己查看）
     * GET /api/reports/personal-summary?userId=&month=yyyy-MM
     */
    @GetMapping("/personal-summary")
    public ResponseEntity<?> personalSummary(@RequestParam int userId,
                                             @RequestParam(required = false) String month) {
        Date reportDate = parseMonthOrDefault(month);
        User user = userDAO.findById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        List<SalesItem> salesData = generateEmployeeSalesData(userId, reportDate);
        double totalAmount = calculateMonthlyTotal(salesData);

        Map<String, Object> resp = new HashMap<>();
        resp.put("user", user);
        resp.put("salesData", salesData);
        resp.put("totalAmount", totalAmount);
        resp.put("reportMonth", new SimpleDateFormat("yyyy-MM").format(reportDate));
        resp.put("now", new Date());
        return ResponseEntity.ok(resp);
    }


    private Date parseMonthOrDefault(String monthStr) {
        if (monthStr == null || monthStr.isEmpty()) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            return cal.getTime();
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            return sdf.parse(monthStr);
        } catch (ParseException e) {
            return new Date();
        }
    }

    private List<SalesItem> generateMonthlySalesData(Date reportDate) {
        List<SalesItem> salesData = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.setTime(reportDate);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();
        
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endDate = cal.getTime();

        String sql = "SELECT oi.name, oi.unit, SUM(oi.quantity) as total_quantity, " +
                "oi.price, SUM(oi.subtotal) as subtotal " +
                "FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "WHERE o.meal_date >= ? AND o.meal_date <= ? " +
                "GROUP BY oi.name, oi.unit, oi.price " +
                "ORDER BY oi.name";

        System.out.println("执行SQL查询...");
        List<SalesItem> results = jdbcTemplate.query(sql, 
            (rs, rowNum) -> new SalesItem(
                rs.getString("name"),
                rs.getString("unit"),
                rs.getInt("total_quantity"),
                rs.getDouble("price"),
                rs.getDouble("subtotal")
            ),
            new java.sql.Timestamp(startDate.getTime()),
            new java.sql.Timestamp(endDate.getTime())
        );
        salesData.addAll(results);
        return salesData;
    }

    private List<SalesItem> generateEmployeeSalesData(int userId, Date reportDate) {
        List<SalesItem> salesData = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.setTime(reportDate);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = cal.getTime();

        String sql = "SELECT oi.name, oi.unit, SUM(oi.quantity) as total_quantity, " +
                "oi.price, SUM(oi.subtotal) as subtotal " +
                "FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "WHERE o.user_id = ? AND o.meal_date BETWEEN ? AND ? " +
                "GROUP BY oi.name, oi.unit, oi.price " +
                "ORDER BY oi.name";

        List<SalesItem> results = jdbcTemplate.query(sql, 
            (rs, rowNum) -> new SalesItem(
                rs.getString("name"),
                rs.getString("unit"),
                rs.getInt("total_quantity"),
                rs.getDouble("price"),
                rs.getDouble("subtotal")
            ),
            userId,
            new java.sql.Date(startDate.getTime()),
            new java.sql.Date(endDate.getTime())
        );
        salesData.addAll(results);
        return salesData;
    }

    private double calculateMonthlyTotal(List<SalesItem> salesData) {
        double total = 0.0;
        for (SalesItem item : salesData) {
            total += item.getSubtotal();
        }
        return total;
    }

    // DTO
    public static class SalesItem {
        private String itemName;
        private String unit;
        private int quantity;
        private double price;
        private double subtotal;

        public SalesItem(String itemName, String unit, int quantity, double price, double subtotal) {
            this.itemName = itemName;
            this.unit = unit;
            this.quantity = quantity;
            this.price = price;
            this.subtotal = subtotal;
        }

        public String getItemName() {
            return itemName;
        }

        public String getUnit() {
            return unit;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return price;
        }

        public double getSubtotal() {
            return subtotal;
        }
    }
}


