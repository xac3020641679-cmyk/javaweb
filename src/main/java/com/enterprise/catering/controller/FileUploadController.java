package com.enterprise.catering.controller;

import com.enterprise.catering.util.Config;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传接口，替代原 FileUploadServlet。
 */
@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(error("文件上传失败：文件为空"));
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            int lastDotIndex = originalFilename.lastIndexOf(".");
            fileExtension = originalFilename.substring(lastDotIndex); // 包含点号，如 .png
        } else {
            fileExtension = ".tmp";
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        String frontendUploadDir = getFrontendUploadDir();
        if (frontendUploadDir == null) {
            String errorMsg = "无法确定前端项目上传目录。请确保前端项目javaweb与后端项目qyct_system在同一父目录(code)下";
            System.err.println("✗ " + errorMsg);
            return ResponseEntity.status(500).body(error(errorMsg));
        }

        // 严格验证路径
        File uploadDirFile = new File(frontendUploadDir);
        String canonicalPath;
        try {
            canonicalPath = uploadDirFile.getCanonicalPath();
        } catch (IOException e) {
            String errorMsg = "无法获取路径的规范形式: " + frontendUploadDir;
            System.err.println("✗ " + errorMsg);
            return ResponseEntity.status(500).body(error(errorMsg));
        }

        if (!canonicalPath.contains("javaweb") || !canonicalPath.contains("public") || !canonicalPath.contains("uploads")) {
            String errorMsg = "路径验证失败！找到的路径不包含javaweb/public/uploads: " + canonicalPath;
            System.err.println("✗ " + errorMsg);
            return ResponseEntity.status(500).body(error(errorMsg));
        }

        if (canonicalPath.contains("qyct_system")) {
            String errorMsg = "路径验证失败！路径包含qyct_system，这是后端目录，不允许保存文件: " + canonicalPath;
            System.err.println("✗ " + errorMsg);
            return ResponseEntity.status(500).body(error(errorMsg));
        }

        File dir = new File(canonicalPath);

        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                return ResponseEntity.status(500).body(error("无法创建上传目录"));
            }
        }

        Path target = new File(dir, uniqueFileName).toPath();

        // 保存文件
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // 验证文件是否真的保存成功
        File savedFile = target.toFile();
        if (!savedFile.exists() || savedFile.length() == 0) {
            return ResponseEntity.status(500).body(error("文件保存失败"));
        }

        String filePath = Config.UPLOAD_PATH + uniqueFileName;

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "success");
        resp.put("filePath", filePath);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFile(@RequestParam("filePath") String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(error("文件路径不能为空"));
        }

        try {
            String frontendUploadDir = getFrontendUploadDir();
            if (frontendUploadDir == null) {
                return ResponseEntity.status(500).body(error("无法确定前端项目上传目录"));
            }

            File uploadDirFile = new File(frontendUploadDir);
            String canonicalPath = uploadDirFile.getCanonicalPath();

            // 从文件路径中提取文件名
            String fileName = filePath;
            if (fileName.startsWith("/uploads/")) {
                fileName = fileName.substring("/uploads/".length());
            } else if (fileName.startsWith("uploads/")) {
                fileName = fileName.substring("uploads/".length());
            }

            File targetFile = new File(canonicalPath, fileName);
            String targetCanonicalPath = targetFile.getCanonicalPath();

            // 安全检查：确保文件在uploads目录内
            if (!targetCanonicalPath.startsWith(canonicalPath)) {
                return ResponseEntity.status(403).body(error("不允许删除uploads目录外的文件"));
            }

            if (targetFile.exists() && targetFile.isFile()) {
                boolean deleted = targetFile.delete();
                if (deleted) {
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("status", "success");
                    resp.put("message", "文件删除成功");
                    return ResponseEntity.ok(resp);
                } else {
                    return ResponseEntity.status(500).body(error("文件删除失败"));
                }
            } else {
                Map<String, Object> resp = new HashMap<>();
                resp.put("status", "success");
                resp.put("message", "文件不存在，视为删除成功");
                return ResponseEntity.ok(resp);
            }
        } catch (Exception e) {
            System.err.println("删除文件时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(error("删除文件失败: " + e.getMessage()));
        }
    }

    /**
     * 获取前端项目上传目录（javaweb/public/uploads）
     */
    private String getFrontendUploadDir() {
        String frontendUploadDir = null;

        try {
            try {
                String classPath = FileUploadController.class.getProtectionDomain()
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
                // 方式1失败，尝试其他方式
            }

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
                    // 方式2失败，尝试其他方式
                }
            }

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

    private Map<String, Object> error(String msg) {
        Map<String, Object> m = new HashMap<>();
        m.put("status", "error");
        m.put("message", msg);
        return m;
    }
}
