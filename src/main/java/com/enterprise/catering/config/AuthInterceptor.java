package com.enterprise.catering.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 认证拦截器：验证用户是否已登录
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    // 不需要认证的路径
    private static final String[] PUBLIC_PATHS = {
        "/api/auth/login",
        "/api/auth/logout",
        "/api/auth/current",
        "/uploads/"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        
        // 检查是否为公开路径
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }
        
        // 检查Session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"未登录或登录已过期，请重新登录\"}");
            return false;
        }
        
        return true;
    }
}

