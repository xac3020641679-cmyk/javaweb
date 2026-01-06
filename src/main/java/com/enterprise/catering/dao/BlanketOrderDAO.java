package com.enterprise.catering.dao;

import com.enterprise.catering.model.BlanketOrder;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


@Repository
public class BlanketOrderDAO extends BaseDAO {

    public boolean generateBlanketOrder(Date date) {
        try {
            // 尝试删除旧数据（如果表存在）
            String deleteSql = "DELETE FROM blanket_orders WHERE date = ?";
            try {
                jdbcTemplate.update(deleteSql, new java.sql.Date(date.getTime()));
            } catch (Exception e) {
                // blanket_orders 表可能不存在，将直接从订单表查询
            }

            // 尝试插入新数据（如果表存在）
            String insertSql = "INSERT INTO blanket_orders (date, item_name, unit, total_quantity, price, subtotal) " +
                    "SELECT o.meal_date, oi.name, oi.unit, SUM(oi.quantity), oi.price, SUM(oi.subtotal) " +
                    "FROM orders o " +
                    "JOIN order_items oi ON o.id = oi.order_id " +
                    "WHERE o.meal_date = ? " +
                    "GROUP BY oi.name, oi.unit, oi.price";
            try {
                jdbcTemplate.update(insertSql, new java.sql.Date(date.getTime()));
                return true;
            } catch (Exception e) {
                // 无法保存到 blanket_orders 表，但查询时会直接从订单表读取
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<BlanketOrder> getBlanketOrdersByDate(Date date) {
        String sql = "SELECT oi.name, oi.unit, SUM(oi.quantity) as total_quantity, " +
                "oi.price, SUM(oi.subtotal) as subtotal " +
                "FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "WHERE o.meal_date = ? " +
                "GROUP BY oi.name, oi.unit, oi.price " +
                "ORDER BY oi.name";
        
        List<BlanketOrder> blanketOrders = jdbcTemplate.query(sql, 
            (rs, rowNum) -> {
                BlanketOrder blanketOrder = new BlanketOrder();
                blanketOrder.setDate(date);
                blanketOrder.setItemName(rs.getString("name"));
                blanketOrder.setUnit(rs.getString("unit"));
                blanketOrder.setTotalQuantity(rs.getInt("total_quantity"));
                blanketOrder.setPrice(rs.getDouble("price"));
                blanketOrder.setSubtotal(rs.getDouble("subtotal"));
                return blanketOrder;
            }, 
            new java.sql.Date(date.getTime()));

        return blanketOrders;
    }

    public double calculateTotalAmount(Date date) {
        String sql = "SELECT SUM(oi.subtotal) AS total " +
                "FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "WHERE o.meal_date = ?";
        Double total = jdbcTemplate.queryForObject(sql, Double.class, new java.sql.Date(date.getTime()));
        return total != null ? total : 0.0;
    }
}
