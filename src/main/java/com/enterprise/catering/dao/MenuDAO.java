package com.enterprise.catering.dao;

import com.enterprise.catering.model.Menu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.Date;
import java.util.List;

@Repository
public class MenuDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<Menu> MENU_ROW_MAPPER = new RowMapper<Menu>() {
        @Override
        public Menu mapRow(ResultSet rs, int rowNum) throws SQLException {
            Menu menu = new Menu();
            menu.setId(rs.getInt("id"));
            menu.setName(rs.getString("name"));
            menu.setDate(rs.getDate("date"));
            menu.setActive(rs.getBoolean("active"));
            menu.setCreatedAt(rs.getTimestamp("created_at"));
            return menu;
        }
    };

    public List<Menu> getAllMenus() {
        try {
            String sql = "SELECT * FROM menus";
            return jdbcTemplate.query(sql, MENU_ROW_MAPPER);
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public List<Menu> getActiveMenusForToday() {
        try {
            String sql = "SELECT * FROM menus WHERE active = TRUE AND date >= CURDATE() ORDER BY date ASC";
            return jdbcTemplate.query(sql, MENU_ROW_MAPPER);
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public Menu getMenuById(int id) {
        try {
            String sql = "SELECT * FROM menus WHERE id = ?";
            List<Menu> menus = jdbcTemplate.query(sql, MENU_ROW_MAPPER, id);
            return menus.isEmpty() ? null : menus.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Menu getActiveMenuByDate(Date date) {
        try {
            String sql = "SELECT * FROM menus WHERE date = ? AND active = TRUE";
            List<Menu> menus = jdbcTemplate.query(sql, MENU_ROW_MAPPER, new java.sql.Date(date.getTime()));
            return menus.isEmpty() ? null : menus.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int addMenu(Menu menu) {
        String sql = "INSERT INTO menus (name, date, active, created_at) VALUES (?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, menu.getName());
            ps.setDate(2, new java.sql.Date(menu.getDate().getTime()));
            ps.setBoolean(3, menu.isActive());
            ps.setTimestamp(4, new java.sql.Timestamp(menu.getCreatedAt().getTime()));
            return ps;
        }, keyHolder);
        
        Number key = keyHolder.getKey();
        if (key != null) {
            return key.intValue();
        } else {
            throw new RuntimeException("创建菜单失败，未获取到生成的ID");
        }
    }

    public void updateMenu(Menu menu) {
        String sql = "UPDATE menus SET name = ?, date = ?, active = ? WHERE id = ?";
        jdbcTemplate.update(sql, 
            menu.getName(), 
            new java.sql.Date(menu.getDate().getTime()), 
            menu.isActive(), 
            menu.getId());
    }

    public void deleteMenu(int id) {
        String sql = "DELETE FROM menus WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
