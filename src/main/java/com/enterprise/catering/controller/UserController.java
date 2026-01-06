package com.enterprise.catering.controller;

import com.enterprise.catering.dao.UserDAO;
import com.enterprise.catering.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

/**
 * 用户管理相关接口
 * 这里假定只有餐厅经理在前端界面中可以访问这些接口。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserDAO userDAO;

    @GetMapping
    public ResponseEntity<List<User>> listUsers() {
        return ResponseEntity.ok(userDAO.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<?> addUser(@RequestBody User user) {
        // 检查餐厅经理和厨房主管的唯一性
        if ("manager".equals(user.getRole())) {
            if (userDAO.existsUserWithRole("manager", null)) {
                return ResponseEntity.badRequest().body("系统中已存在餐厅经理，只能有一个餐厅经理");
            }
        } else if ("kitchen_chief".equals(user.getRole())) {
            if (userDAO.existsUserWithRole("kitchen_chief", null)) {
                return ResponseEntity.badRequest().body("系统中已存在厨房主管，只能有一个厨房主管");
            }
        }
        
        boolean success = userDAO.addUser(user);
        return success ? ResponseEntity.ok().build() :
                ResponseEntity.badRequest().body("add_failed");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody User user) {
        user.setId(id);
        
        // 检查餐厅经理和厨房主管的唯一性（排除当前编辑的用户）
        if ("manager".equals(user.getRole())) {
            if (userDAO.existsUserWithRole("manager", id)) {
                return ResponseEntity.badRequest().body("系统中已存在餐厅经理，只能有一个餐厅经理");
            }
        } else if ("kitchen_chief".equals(user.getRole())) {
            if (userDAO.existsUserWithRole("kitchen_chief", id)) {
                return ResponseEntity.badRequest().body("系统中已存在厨房主管，只能有一个厨房主管");
            }
        }
        
        boolean success = userDAO.updateUser(user);
        return success ? ResponseEntity.ok().build() :
                ResponseEntity.badRequest().body("update_failed");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) {
        boolean success = userDAO.deleteUser(id);
        return success ? ResponseEntity.ok().build() :
                ResponseEntity.badRequest().body("delete_failed");
    }

    /**
     * 批量导入用户：前端传入一整个大字符串（与原 JSP 规则一致）
     */
    @PostMapping("/bulk-import")
    public ResponseEntity<?> bulkImport(@RequestBody BulkImportRequest body) {
        String bulkData = body.getBulkData();
        if (bulkData == null || bulkData.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("empty_bulk_data");
        }

        String[] lines = bulkData.split("\\r?\\n");
        int successCount = 0;
        int failCount = 0;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split("[,，]");
            if (parts.length < 7) {
                failCount++;
                continue;
            }

            String name = parts[0].trim();
            String loginName = parts[1].trim();
            String password = parts[2].trim();
            String phone = parts[3].trim();
            String department = parts[4].trim();
            String workstation = parts[5].trim();
            String role = parts[6].trim();

            if (!"manager".equals(role) &&
                    !"kitchen_chief".equals(role) &&
                    !"delivery_staff".equals(role) &&
                    !"finance".equals(role) &&
                    !"employee".equals(role)) {
                failCount++;
                continue;
            }

            // 检查餐厅经理和厨房主管的唯一性
            if ("manager".equals(role)) {
                if (userDAO.existsUserWithRole("manager", null)) {
                    failCount++;
                    continue;
                }
            } else if ("kitchen_chief".equals(role)) {
                if (userDAO.existsUserWithRole("kitchen_chief", null)) {
                    failCount++;
                    continue;
                }
            }
            
            User user = new User(name, loginName, password, phone, department, workstation, role);
            boolean success = userDAO.addUser(user);
            if (success) {
                successCount++;
            } else {
                failCount++;
            }
        }

        BulkImportResult result = new BulkImportResult();
        result.setSuccessCount(successCount);
        result.setFailCount(failCount);
        return ResponseEntity.ok(result);
    }

    public static class BulkImportRequest {
        private String bulkData;

        public String getBulkData() {
            return bulkData;
        }

        public void setBulkData(String bulkData) {
            this.bulkData = bulkData;
        }
    }

    public static class BulkImportResult {
        private int successCount;
        private int failCount;

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public int getFailCount() {
            return failCount;
        }

        public void setFailCount(int failCount) {
            this.failCount = failCount;
        }
    }
}


