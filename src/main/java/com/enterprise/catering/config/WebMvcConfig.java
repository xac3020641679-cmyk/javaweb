package com.enterprise.catering.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置类，用于配置静态资源映射和拦截器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的静态资源映射（映射到前端项目的public/uploads目录）
        try {
            String frontendUploadDir = getFrontendUploadDir();
            
            if (frontendUploadDir == null) {
                System.err.println("WebMvcConfig - 错误：无法确定前端项目上传目录");
                return;
            }

            // 确保路径以 / 结尾
            if (!frontendUploadDir.endsWith(File.separator)) {
                frontendUploadDir = frontendUploadDir + File.separator;
            }

            // 将 Windows 路径分隔符转换为 /（用于file:协议）
            String fileUrlPath = frontendUploadDir.replace("\\", "/");

            // Windows路径特殊处理
            if (fileUrlPath.length() > 2 && fileUrlPath.charAt(1) == ':') {
                fileUrlPath = "///" + fileUrlPath;
            } else {
                fileUrlPath = "///" + fileUrlPath;
            }

            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:" + fileUrlPath);
        } catch (Exception e) {
            System.err.println("配置静态资源映射失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取前端项目上传目录（与FileUploadController保持一致）
     */
    private String getFrontendUploadDir() {
        String frontendUploadDir = null;

        try {
            // 方式1：从类路径绝对定位
            try {
                String classPath = WebMvcConfig.class.getProtectionDomain()
                        .getCodeSource().getLocation().getPath();

                if (classPath.startsWith("file:/")) {
                    classPath = classPath.substring(6);
                }
                if (classPath.startsWith("/")) {
                    classPath = classPath.substring(1);
                }
                classPath = java.net.URLDecoder.decode(classPath, "UTF-8");

                File classFile = new File(classPath);
                File temp = classFile;
                int maxDepth = 10;
                int depth = 0;
                while (temp != null && depth < maxDepth) {
                    if (temp.getName().equals("code")) {
                        File javawebDir = new File(temp, "javaweb");
                        if (javawebDir.exists() && javawebDir.isDirectory()) {
                            File publicDir = new File(javawebDir, "public");
                            File uploadsDir = new File(publicDir, "uploads");
                            frontendUploadDir = uploadsDir.getCanonicalPath();
                            break;
                        }
                    }
                    temp = temp.getParentFile();
                    depth++;
                }
            } catch (Exception e) {
                // 忽略
            }

            // 方式2：从当前工作目录找
            if (frontendUploadDir == null) {
                try {
                    String currentPath = new File(".").getCanonicalPath();
                    File currentDir = new File(currentPath);

                    if (currentDir.getName().equals("qyct_system")) {
                        File parent = currentDir.getParentFile();
                        if (parent != null) {
                            File javawebDir = new File(parent, "javaweb");
                            if (javawebDir.exists() && javawebDir.isDirectory()) {
                                File publicDir = new File(javawebDir, "public");
                                File uploadsDir = new File(publicDir, "uploads");
                                frontendUploadDir = uploadsDir.getCanonicalPath();
                            }
                        }
                    } else if (currentDir.getName().equals("code")) {
                        File javawebDir = new File(currentDir, "javaweb");
                        if (javawebDir.exists() && javawebDir.isDirectory()) {
                            File publicDir = new File(javawebDir, "public");
                            File uploadsDir = new File(publicDir, "uploads");
                            frontendUploadDir = uploadsDir.getCanonicalPath();
                        }
                    }
                } catch (Exception e) {
                    // 忽略
                }
            }

            // 方式3：使用系统属性
            if (frontendUploadDir == null) {
                String javawebPath = System.getProperty("javaweb.path");
                if (javawebPath != null && !javawebPath.isEmpty()) {
                    File javawebDir = new File(javawebPath);
                    if (javawebDir.exists() && javawebDir.isDirectory()) {
                        File publicDir = new File(javawebDir, "public");
                        File uploadsDir = new File(publicDir, "uploads");
                        frontendUploadDir = uploadsDir.getCanonicalPath();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("获取前端上传目录失败: " + e.getMessage());
        }

        return frontendUploadDir;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/logout", "/api/auth/current");
    }
}
