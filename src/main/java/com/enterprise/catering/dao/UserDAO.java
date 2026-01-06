package com.enterprise.catering.dao;

import com.enterprise.catering.model.User;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


@Repository
public class UserDAO extends BaseDAO {

    private static final RowMapper<User> USER_ROW_MAPPER = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));
            user.setLoginName(rs.getString("login_name"));
            user.setPassword(rs.getString("password"));
            user.setPhone(rs.getString("phone"));
            user.setDepartment(rs.getString("department"));
            user.setWorkstation(rs.getString("workstation"));
            user.setRole(rs.getString("role"));
            return user;
        }
    };

    public User login(String loginName, String password) {
        String sql = "SELECT * FROM users WHERE login_name = ? AND password = ?";
        List<User> users = jdbcTemplate.query(sql, USER_ROW_MAPPER, loginName, password);
        return users.isEmpty() ? null : users.get(0);
    }

    public User authenticate(String loginName, String password) {
        return login(loginName, password);
    }

    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, USER_ROW_MAPPER, id);
        return users.isEmpty() ? null : users.get(0);
    }

    public boolean addUser(User user) {
        String sql = "INSERT INTO users (name, login_name, password, phone, department, workstation, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int result = jdbcTemplate.update(sql, 
            user.getName(), 
            user.getLoginName(), 
            user.getPassword(), 
            user.getPhone(), 
            user.getDepartment(), 
            user.getWorkstation(), 
            user.getRole());
        return result > 0;
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE users SET name=?, login_name=?, password=?, phone=?, department=?, workstation=?, role=? WHERE id=?";
        int result = jdbcTemplate.update(sql, 
            user.getName(), 
            user.getLoginName(), 
            user.getPassword(), 
            user.getPhone(), 
            user.getDepartment(), 
            user.getWorkstation(), 
            user.getRole(), 
            user.getId());
        return result > 0;
    }

    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id=?";
        int result = jdbcTemplate.update(sql, id);
        return result > 0;
    }

    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY id";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public List<User> getUsersByRole(String role) {
        String sql = "SELECT * FROM users WHERE role=? ORDER BY id";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER, role);
    }


    public boolean existsUserWithRole(String role, Integer excludeId) {
        String sql;
        Integer count;
        if (excludeId != null && excludeId > 0) {
            sql = "SELECT COUNT(*) FROM users WHERE role=? AND id != ?";
            count = jdbcTemplate.queryForObject(sql, Integer.class, role, excludeId);
        } else {
            sql = "SELECT COUNT(*) FROM users WHERE role=?";
            count = jdbcTemplate.queryForObject(sql, Integer.class, role);
        }
        return count != null && count > 0;
    }
}
