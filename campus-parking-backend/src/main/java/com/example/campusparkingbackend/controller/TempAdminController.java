// src/main/java/com/example/campusparkingbackend/controller/TempAdminController.java
package com.example.campusparkingbackend.controller;

import com.example.campusparkingbackend.entity.User;
import com.example.campusparkingbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 临时管理员控制器
 * 用于系统维护和管理员账户管理
 * 注意：这些接口应该在生产环境中禁用或加强权限控制
 */
@RestController
@RequestMapping("/api/temp-admin")
@CrossOrigin(origins = "*")
public class TempAdminController {

    @Autowired
    private UserService userService;

    /**
     * 重置管理员密码
     */
    @PostMapping("/reset-admin-password")
    public ResponseEntity<?> resetAdminPassword(@RequestBody ResetPasswordRequest request) {
        try {
            User admin = userService.resetAdminPassword(request.getAdminId(), request.getNewPassword());
            return ResponseEntity.ok("管理员密码重置成功");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 重置用户密码（管理员权限）
     */
    @PostMapping("/reset-user-password")
    public ResponseEntity<?> resetUserPassword(@RequestBody ResetUserPasswordRequest request) {
        try {
            User user = userService.resetUserPassword(request.getUserId(), request.getNewPassword());
            return ResponseEntity.ok("用户密码重置成功");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 创建新的管理员账户
     */
    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdminUser(@RequestBody CreateAdminRequest request) {
        try {
            User admin = userService.createAdminUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getRealName(),
                    request.getPhone(),
                    request.getEmail()
            );
            return ResponseEntity.ok(admin);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取所有管理员账户
     */
    @GetMapping("/admins")
    public ResponseEntity<List<User>> getAllAdminUsers() {
        try {
            List<User> admins = userService.getAllAdminUsers();
            return ResponseEntity.ok(admins);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 修复所有管理员账户状态（确保都是已审核状态）
     */
    @PostMapping("/fix-all-admins")
    public ResponseEntity<?> fixAllAdminUsers() {
        try {
            List<User> admins = userService.getAllAdminUsers();
            int fixedCount = 0;

            for (User admin : admins) {
                if (admin.getUserStatus() != User.UserStatus.APPROVED) {
                    admin.setUserStatus(User.UserStatus.APPROVED);
                    admin.setUpdatedAt(java.time.LocalDateTime.now());
                    userService.updateUser(admin.getId(), admin);
                    fixedCount++;
                }
            }

            return ResponseEntity.ok("已修复 " + fixedCount + " 个管理员账户状态");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("修复管理员账户状态失败: " + e.getMessage());
        }
    }

    /**
     * 紧急系统状态检查
     */
    @GetMapping("/system-status")
    public ResponseEntity<SystemStatus> getSystemStatus() {
        try {
            SystemStatus status = new SystemStatus();

            // 获取管理员数量
            List<User> admins = userService.getAllAdminUsers();
            status.setAdminCount(admins.size());

            // 获取活跃管理员数量
            long activeAdmins = admins.stream()
                    .filter(admin -> admin.getUserStatus() == User.UserStatus.APPROVED)
                    .count();
            status.setActiveAdminCount(activeAdmins);

            // 获取总用户数
            status.setTotalUsers(userService.getUserCount());

            status.setStatus("正常");
            status.setTimestamp(java.time.LocalDateTime.now());

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            SystemStatus errorStatus = new SystemStatus();
            errorStatus.setStatus("错误: " + e.getMessage());
            errorStatus.setTimestamp(java.time.LocalDateTime.now());
            return ResponseEntity.badRequest().body(errorStatus);
        }
    }

    // 请求体类
    public static class ResetPasswordRequest {
        private Long adminId;
        private String newPassword;

        // Getters and Setters
        public Long getAdminId() { return adminId; }
        public void setAdminId(Long adminId) { this.adminId = adminId; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class ResetUserPasswordRequest {
        private Long userId;
        private String newPassword;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class CreateAdminRequest {
        private String username;
        private String password;
        private String realName;
        private String phone;
        private String email;

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // 系统状态类 - 使用 long 类型
    public static class SystemStatus {
        private String status;
        private long adminCount;
        private long activeAdminCount;
        private long totalUsers;
        private java.time.LocalDateTime timestamp;

        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public long getAdminCount() { return adminCount; }
        public void setAdminCount(long adminCount) { this.adminCount = adminCount; }
        public long getActiveAdminCount() { return activeAdminCount; }
        public void setActiveAdminCount(long activeAdminCount) { this.activeAdminCount = activeAdminCount; }
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        public java.time.LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(java.time.LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}
