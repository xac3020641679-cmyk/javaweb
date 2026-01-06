package com.enterprise.catering.controller;

import com.enterprise.catering.dao.UserDAO;
import com.enterprise.catering.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录认证控制器：
 * 使用Session和Cookie进行用户认证管理
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserDAO userDAO;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, 
                                    HttpServletRequest httpRequest,
                                    HttpServletResponse httpResponse) {
        if (request.getLoginName() == null || request.getLoginName().isEmpty()
                || request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body("missing");
        }
        User user = userDAO.authenticate(request.getLoginName(), request.getPassword());
        if (user == null) {
            return ResponseEntity.status(401).body("invalid");
        }
        
        // 创建Session并存储用户信息
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getName());
        session.setAttribute("userRole", user.getRole());
        session.setAttribute("user", user);
        
        // 设置Session超时时间（30分钟）
        session.setMaxInactiveInterval(30 * 60);
        
        // 返回用户信息（不包含敏感信息）
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("name", user.getName());
        userInfo.put("role", user.getRole());
        userInfo.put("phone", user.getPhone());
        userInfo.put("workstation", user.getWorkstation());
        
        return ResponseEntity.ok(userInfo);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok("logout_success");
    }
    
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return ResponseEntity.status(401).body("not_authenticated");
        }
        User user = (User) session.getAttribute("user");
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("name", user.getName());
        userInfo.put("role", user.getRole());
        userInfo.put("phone", user.getPhone());
        userInfo.put("workstation", user.getWorkstation());
        return ResponseEntity.ok(userInfo);
    }

    public static class LoginRequest {
        private String loginName;
        private String password;

        public String getLoginName() {
            return loginName;
        }

        public void setLoginName(String loginName) {
            this.loginName = loginName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}


