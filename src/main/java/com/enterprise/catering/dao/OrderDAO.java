package com.enterprise.catering.dao;

import com.enterprise.catering.model.Order;
import com.enterprise.catering.model.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Repository
public class OrderDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<Order> ORDER_ROW_MAPPER = new RowMapper<Order>() {
        @Override
        public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
            Order order = new Order();
            order.setId(rs.getInt("id"));
            order.setUserId(rs.getInt("user_id"));
            order.setUserName(rs.getString("user_name"));
            order.setPhone(rs.getString("phone"));
            order.setWorkLocation(rs.getString("work_location"));
            order.setOrderTime(rs.getTimestamp("order_time"));
            order.setMealDate(rs.getDate("meal_date"));
            order.setTotalPrice(rs.getDouble("total_price"));
            order.setCreatedAt(rs.getTimestamp("created_at"));
            return order;
        }
    };

    private static final RowMapper<OrderItem> ORDER_ITEM_ROW_MAPPER = new RowMapper<OrderItem>() {
        @Override
        public OrderItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            OrderItem item = new OrderItem();
            item.setId(rs.getInt("id"));
            item.setOrderId(rs.getInt("order_id"));
            item.setMenuItemId(rs.getInt("menu_item_id"));
            item.setName(rs.getString("name"));
            item.setUnit(rs.getString("unit"));
            item.setQuantity(rs.getInt("quantity"));
            item.setPrice(rs.getDouble("price"));
            item.setSubtotal(rs.getDouble("subtotal"));
            return item;
        }
    };

    public void addOrder(Order order) {
        String sql = "INSERT INTO orders (user_id, user_name, phone, work_location, meal_date, total_price) VALUES (?, ?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, order.getUserId());
            ps.setString(2, order.getUserName());
            ps.setString(3, order.getPhone());
            ps.setString(4, order.getWorkLocation());
            ps.setDate(5, new java.sql.Date(order.getMealDate().getTime()));
            ps.setDouble(6, order.getTotalPrice());
            return ps;
        }, keyHolder);
        
        Number key = keyHolder.getKey();
        if (key != null) {
            order.setId(key.intValue());
        } else {
            throw new RuntimeException("Creating order failed, no ID obtained.");
        }
    }

    public void addOrderItem(OrderItem orderItem) {
        String sql = "INSERT INTO order_items (order_id, menu_item_id, name, unit, quantity, price, subtotal) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, 
            orderItem.getOrderId(),
            orderItem.getMenuItemId(),
            orderItem.getName(),
            orderItem.getUnit(),
            orderItem.getQuantity(),
            orderItem.getPrice(),
            orderItem.getSubtotal());
    }

    public List<Order> getOrdersByUserId(int userId) {
        try {
            String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_time DESC";
            return jdbcTemplate.query(sql, ORDER_ROW_MAPPER, userId);
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public List<Order> getAllOrders() {
        try {
            String sql = "SELECT * FROM orders ORDER BY order_time DESC";
            return jdbcTemplate.query(sql, ORDER_ROW_MAPPER);
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 根据日期获取该日所有订单（按用户名排序）
     */
    public List<Order> getOrdersByDate(Date date) {
        try {
            String sql = "SELECT * FROM orders WHERE meal_date = ? ORDER BY user_name";
            return jdbcTemplate.query(sql, ORDER_ROW_MAPPER, new java.sql.Date(date.getTime()));
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public List<Order> getOrdersByUserIdAndMonth(int userId, Date monthDate) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(monthDate);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            Date startDate = cal.getTime();
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date endDate = cal.getTime();

            String sql = "SELECT * FROM orders WHERE user_id = ? AND meal_date BETWEEN ? AND ? " +
                    "ORDER BY meal_date ASC, order_time ASC";
            return jdbcTemplate.query(sql, ORDER_ROW_MAPPER, 
                userId, 
                new java.sql.Date(startDate.getTime()), 
                new java.sql.Date(endDate.getTime()));
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public Order getOrderById(int orderId) {
        try {
            String sql = "SELECT * FROM orders WHERE id = ?";
            List<Order> orders = jdbcTemplate.query(sql, ORDER_ROW_MAPPER, orderId);
            return orders.isEmpty() ? null : orders.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<OrderItem> getOrderItemsByOrderId(int orderId) {
        try {
            String sql = "SELECT * FROM order_items WHERE order_id = ?";
            return jdbcTemplate.query(sql, ORDER_ITEM_ROW_MAPPER, orderId);
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public boolean hasOrderByUserIdAndDate(int userId, Date date) {
        try {
            String sql = "SELECT COUNT(*) FROM orders WHERE user_id = ? AND meal_date = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, new java.sql.Date(date.getTime()));
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
