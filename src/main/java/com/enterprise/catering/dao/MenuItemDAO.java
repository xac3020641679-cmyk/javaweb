package com.enterprise.catering.dao;

import com.enterprise.catering.model.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class MenuItemDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<MenuItem> MENU_ITEM_ROW_MAPPER = new RowMapper<MenuItem>() {
        @Override
        public MenuItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            MenuItem menuItem = new MenuItem();
            menuItem.setId(rs.getInt("id"));
            menuItem.setMenuId(rs.getInt("menu_id"));
            menuItem.setRecipeId(rs.getInt("recipe_id"));
            menuItem.setName(rs.getString("name"));
            menuItem.setImage(rs.getString("image"));
            menuItem.setUnit(rs.getString("unit"));
            menuItem.setPrice(rs.getDouble("price"));
            menuItem.setCreatedAt(rs.getTimestamp("created_at"));
            return menuItem;
        }
    };

    public List<MenuItem> getAllMenuItems() {
        try {
            String sql = "SELECT * FROM menu_items";
            return jdbcTemplate.query(sql, MENU_ITEM_ROW_MAPPER);
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public MenuItem getMenuItemById(int id) {
        try {
            String sql = "SELECT * FROM menu_items WHERE id = ?";
            List<MenuItem> items = jdbcTemplate.query(sql, MENU_ITEM_ROW_MAPPER, id);
            return items.isEmpty() ? null : items.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<MenuItem> getActiveMenuItems() {
        try {
            String sql = "SELECT mi.* FROM menu_items mi " +
                    "JOIN menus m ON mi.menu_id = m.id " +
                    "WHERE m.active = TRUE AND m.date = CURDATE()";
            return jdbcTemplate.query(sql, MENU_ITEM_ROW_MAPPER);
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public List<MenuItem> getMenuItemsByMenuId(int menuId) {
        try {
            String sql = "SELECT * FROM menu_items WHERE menu_id = ?";
            return jdbcTemplate.query(sql, MENU_ITEM_ROW_MAPPER, menuId);
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public void addMenuItem(MenuItem menuItem) {
        String sql = "INSERT INTO menu_items (menu_id, recipe_id, name, image, unit, price) VALUES (?, ?, ?, ?, ?, ?)";
        int rowsAffected = jdbcTemplate.update(sql, 
            menuItem.getMenuId(),
            menuItem.getRecipeId(),
            menuItem.getName(),
            menuItem.getImage() != null ? menuItem.getImage() : "",
            menuItem.getUnit(),
            menuItem.getPrice());
    }

    public void updateMenuItem(MenuItem menuItem) {
        String sql = "UPDATE menu_items SET menu_id = ?, recipe_id = ?, name = ?, image = ?, unit = ?, price = ? WHERE id = ?";
        jdbcTemplate.update(sql, 
            menuItem.getMenuId(),
            menuItem.getRecipeId(),
            menuItem.getName(),
            menuItem.getImage() != null ? menuItem.getImage() : "",
            menuItem.getUnit(),
            menuItem.getPrice(),
            menuItem.getId());
    }

    public void deleteMenuItem(int menuItemId) {
        String sql = "DELETE FROM menu_items WHERE id = ?";
        jdbcTemplate.update(sql, menuItemId);
    }

    public void deleteMenuItemsByMenuId(int menuId) {
        String sql = "DELETE FROM menu_items WHERE menu_id = ?";
        jdbcTemplate.update(sql, menuId);
    }
}
