package com.example.campusparkingbackend.controller;

import com.example.campusparkingbackend.dto.ApiResponse;
import com.example.campusparkingbackend.entity.User;
import com.example.campusparkingbackend.service.UserService;
import com.example.campusparkingbackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取所有用户（管理员权限）
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(@RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 开始获取所有用户 ===");
            System.out.println("原始Token: " + token);

            String cleanedToken = cleanToken(token);
            System.out.println("清理后Token: " + cleanedToken);

            boolean adminCheck = isAdmin(token);
            System.out.println("管理员检查结果: " + adminCheck);

            if (!adminCheck) {
                System.out.println("❌ 权限不足，拒绝访问");
                return ResponseEntity.status(403).build();
            }

            List<User> users = userService.getAllUsers();
            System.out.println("✅ 成功获取用户数量: " + users.size());

            // 打印前几个用户信息用于调试
            if (!users.isEmpty()) {
                System.out.println("前3个用户信息:");
                for (int i = 0; i < Math.min(3, users.size()); i++) {
                    User user = users.get(i);
                    System.out.println("用户 " + (i+1) + ": ID=" + user.getId() +
                            ", 用户名=" + user.getUsername() +
                            ", 类型=" + user.getUserType());
                }
            } else {
                System.out.println("⚠️ 用户列表为空");
            }

            return ResponseEntity.ok(users);
        } catch (Exception e) {
            System.err.println("❌ 获取用户列表失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id,
                                            @RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 根据ID获取用户 ===");
            System.out.println("请求的用户ID: " + id);

            // 验证权限：用户只能查看自己的信息，管理员可以查看所有
            Long currentUserId = getUserIdFromToken(token);
            boolean isAdminUser = isAdmin(token);

            System.out.println("当前用户ID: " + currentUserId);
            System.out.println("是否是管理员: " + isAdminUser);

            if (!isAdminUser && !currentUserId.equals(id)) {
                System.out.println("❌ 权限不足，用户只能查看自己的信息");
                return ResponseEntity.status(403).build();
            }

            User user = userService.getUserWithDetails(id);
            if (user != null) {
                System.out.println("✅ 成功获取用户: " + user.getUsername());
                return ResponseEntity.ok(user);
            } else {
                System.out.println("❌ 用户不存在，ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            System.err.println("❌ 获取用户失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 创建用户（注册）
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            System.out.println("=== 用户注册 ===");
            System.out.println("注册用户名: " + user.getUsername());
            System.out.println("用户类型: " + user.getUserType());

            User savedUser = userService.createUser(user);
            System.out.println("✅ 用户注册成功，ID: " + savedUser.getId());
            return ResponseEntity.ok(savedUser);
        } catch (RuntimeException e) {
            System.err.println("❌ 用户注册失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @RequestBody User userDetails,
                                        @RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 更新用户信息 ===");
            System.out.println("目标用户ID: " + id);

            // 验证权限：用户只能修改自己的信息，管理员可以修改所有
            Long currentUserId = getUserIdFromToken(token);
            boolean isAdminUser = isAdmin(token);

            System.out.println("当前用户ID: " + currentUserId);
            System.out.println("是否是管理员: " + isAdminUser);

            if (!isAdminUser && !currentUserId.equals(id)) {
                System.out.println("❌ 无权修改此用户信息");
                return ResponseEntity.status(403).body("无权修改此用户信息");
            }

            User updatedUser = userService.updateUser(id, userDetails);
            System.out.println("✅ 用户信息更新成功");
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            System.err.println("❌ 更新用户信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 删除用户（管理员权限）- 修复版本
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId,
                                        @RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 删除用户 ===");
            System.out.println("目标用户ID: " + userId);

            // 验证管理员权限
            String cleanToken = token.replace("Bearer ", "").trim();
            if (!jwtUtil.validateToken(cleanToken)) {
                return ResponseEntity.status(401).body("Token无效");
            }

            String role = jwtUtil.getRoleFromToken(cleanToken);
            Long currentUserId = jwtUtil.getUserIdFromToken(cleanToken);

            System.out.println("Token中的角色: " + role + ", 当前用户ID: " + currentUserId + ", 是否是管理员: " + "ADMIN".equals(role));

            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("权限不足，需要管理员权限");
            }

            // 调用修复后的删除方法
            userService.deleteUser(userId, currentUserId);

            return ResponseEntity.ok(new ApiResponse(true, "用户删除成功", null));

        } catch (RuntimeException e) {
            System.out.println("❌ 删除用户失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            System.out.println("❌ 删除用户异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ApiResponse(false, "删除用户失败", null));
        }
    }
    /**
     * 重置用户密码（管理员权限）
     */
    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<?> resetUserPassword(@PathVariable Long userId,
                                               @RequestHeader("Authorization") String token) {
        try {
            // 检查管理员权限
            String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("权限不足，需要管理员权限");
            }

            User user = userService.resetUserPassword(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "密码重置成功",
                    "newPassword", "123456" // 返回默认密码用于显示
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    /**
     * 审核用户（管理员权限）
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveUser(@PathVariable Long id,
                                         @RequestBody ApproveRequest request,
                                         @RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 审核用户 ===");
            System.out.println("目标用户ID: " + id);
            System.out.println("审核结果: " + request.getApproved());

            if (!isAdmin(token)) {
                System.out.println("❌ 需要管理员权限");
                return ResponseEntity.status(403).body("需要管理员权限");
            }

            User user = userService.approveUser(id, request.getApproved(), request.getReviewComment());
            System.out.println("✅ 用户审核完成，新状态: " + user.getUserStatus());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            System.err.println("❌ 审核用户失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 批量审核用户（管理员权限）
     */
    @PostMapping("/batch-approve")
    public ResponseEntity<?> batchApproveUsers(@RequestBody BatchApproveRequest request,
                                               @RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 批量审核用户 ===");
            System.out.println("用户ID列表: " + request.getUserIds());
            System.out.println("审核结果: " + request.getApproved());

            if (!isAdmin(token)) {
                System.out.println("❌ 需要管理员权限");
                return ResponseEntity.status(403).body("需要管理员权限");
            }

            List<User> users = userService.batchApproveUsers(request.getUserIds(), request.getApproved());
            System.out.println("✅ 批量审核完成，影响用户数: " + users.size());
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            System.err.println("❌ 批量审核用户失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取待审核用户（管理员权限）
     */
    @GetMapping("/pending")
    public ResponseEntity<List<User>> getPendingUsers(@RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 获取待审核用户 ===");

            if (!isAdmin(token)) {
                System.out.println("❌ 权限不足");
                return ResponseEntity.status(403).build();
            }
            List<User> users = userService.getPendingUsers();
            System.out.println("✅ 获取待审核用户数量: " + users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            System.err.println("❌ 获取待审核用户失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
    /**
     * 获取校内待审核用户（管理员权限）
     */
    @GetMapping("/pending/campus")
    public ResponseEntity<List<User>> getCampusPendingUsers(
            @RequestParam(required = false) String type,
            @RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 获取校内待审核用户 ===");
            System.out.println("用户类型过滤: " + type);

            if (!isAdmin(token)) {
                System.out.println("❌ 权限不足");
                return ResponseEntity.status(403).build();
            }

            List<User> users;
            if (type != null && !type.isEmpty()) {
                try {
                    // 根据类型过滤
                    User.UserType userType = User.UserType.valueOf(type.toUpperCase());
                    users = userService.getPendingUsersByType(userType);
                } catch (IllegalArgumentException e) {
                    System.err.println("❌ 无效的用户类型: " + type);
                    return ResponseEntity.badRequest().body(null);
                }
            } else {
                // 获取所有校内待审核用户（排除校外用户）
                users = userService.getPendingUsers().stream()
                        .filter(user -> user.getUserType() != User.UserType.EXTERNAL_USER)
                        .collect(Collectors.toList());
            }

            System.out.println("✅ 获取校内待审核用户数量: " + users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            System.err.println("❌ 获取校内待审核用户失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 根据状态获取用户（管理员权限）
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<User>> getUsersByStatus(@PathVariable String status,
                                                       @RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 根据状态获取用户 ===");
            System.out.println("状态: " + status);

            if (!isAdmin(token)) {
                System.out.println("❌ 权限不足");
                return ResponseEntity.status(403).build();
            }

            User.UserStatus userStatus;
            try {
                userStatus = User.UserStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("❌ 无效的用户状态: " + status);
                return ResponseEntity.badRequest().body(null);
            }

            List<User> users = userService.getUsersByStatus(userStatus);
            System.out.println("✅ 获取用户数量: " + users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            System.err.println("❌ 根据状态获取用户失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 根据用户类型获取用户（管理员权限）
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<User>> getUsersByType(@PathVariable String type,
                                                     @RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 根据用户类型获取用户 ===");
            System.out.println("类型: " + type);

            if (!isAdmin(token)) {
                System.out.println("❌ 权限不足");
                return ResponseEntity.status(403).build();
            }

            User.UserType userType;
            try {
                userType = User.UserType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("❌ 无效的用户类型: " + type);
                return ResponseEntity.badRequest().body(null);
            }

            List<User> users = userService.getUsersByType(userType);
            System.out.println("✅ 获取用户数量: " + users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            System.err.println("❌ 根据用户类型获取用户失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 获取用户统计信息（管理员权限）
     */
    @GetMapping("/stats")
    public ResponseEntity<UserService.UserStatistics> getUserStatistics(@RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 获取用户统计信息 ===");

            if (!isAdmin(token)) {
                System.out.println("❌ 权限不足");
                return ResponseEntity.status(403).build();
            }

            UserService.UserStatistics stats = userService.getUserStatistics();
            System.out.println("✅ 统计信息 - 总用户: " + stats.getTotalUsers() +
                    ", 待审核: " + stats.getPendingUsers() +
                    ", 已审核: " + stats.getApprovedUsers() +
                    ", 已拒绝: " + stats.getRejectedUsers());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("❌ 获取用户统计信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }


    /**
     * 充值（管理员权限）
     */
    @PostMapping("/{id}/recharge")
    public ResponseEntity<?> recharge(@PathVariable Long id,
                                      @RequestBody RechargeRequest request,
                                      @RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 用户充值 ===");
            System.out.println("用户ID: " + id);
            System.out.println("充值金额: " + request.getAmount());

            if (!isAdmin(token)) {
                System.out.println("❌ 需要管理员权限");
                return ResponseEntity.status(403).body("需要管理员权限");
            }

            User user = userService.recharge(id, request.getAmount());
            System.out.println("✅ 充值成功，新余额: " + user.getBalance());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            System.err.println("❌ 充值失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable Long id,
                                            @RequestBody ChangePasswordRequest request,
                                            @RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 修改密码 ===");
            System.out.println("目标用户ID: " + id);

            // 验证权限：用户只能修改自己的密码
            Long currentUserId = getUserIdFromToken(token);
            System.out.println("当前用户ID: " + currentUserId);

            if (!currentUserId.equals(id)) {
                System.out.println("❌ 无权修改此用户密码");
                return ResponseEntity.status(403).body("无权修改此用户密码");
            }

            User user = userService.changePassword(id, request.getNewPassword());
            System.out.println("✅ 密码修改成功");
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            System.err.println("❌ 修改密码失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    /**
     * 验证原密码（用于修改密码前的验证）
     */
    @PostMapping("/{id}/verify-password")
    public ResponseEntity<?> verifyPassword(@PathVariable Long id,
                                            @RequestBody VerifyPasswordRequest request,
                                            @RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 验证原密码 ===");
            System.out.println("目标用户ID: " + id);

            // 验证权限：用户只能验证自己的密码
            Long currentUserId = getUserIdFromToken(token);
            System.out.println("当前用户ID: " + currentUserId);

            if (!currentUserId.equals(id)) {
                System.out.println("❌ 无权验证此用户密码");
                return ResponseEntity.status(403).body("无权验证此用户密码");
            }

            boolean isValid = userService.verifyPassword(id, request.getPassword());

            if (isValid) {
                System.out.println("✅ 原密码验证成功");
                return ResponseEntity.ok().build();
            } else {
                System.out.println("❌ 原密码验证失败");
                return ResponseEntity.status(401).body("密码错误");
            }
        } catch (RuntimeException e) {
            System.err.println("❌ 验证密码失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 修改密码（需要原密码验证）- 新版
     */
    @PostMapping("/{id}/change-password-verify")
    public ResponseEntity<?> changePasswordWithVerify(@PathVariable Long id,
                                                      @RequestBody ChangePasswordVerifyRequest request,
                                                      @RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 验证并修改密码 ===");
            System.out.println("目标用户ID: " + id);
            System.out.println("新密码长度: " + (request.getNewPassword() != null ? request.getNewPassword().length() : 0));

            // 验证权限：用户只能修改自己的密码
            Long currentUserId = getUserIdFromToken(token);
            System.out.println("当前用户ID: " + currentUserId);

            if (!currentUserId.equals(id)) {
                System.out.println("❌ 无权修改此用户密码");
                return ResponseEntity.status(403).body("无权修改此用户密码");
            }

            // 验证原密码
            boolean oldPasswordValid = userService.verifyPassword(id, request.getOldPassword());
            if (!oldPasswordValid) {
                System.out.println("❌ 原密码错误");
                return ResponseEntity.badRequest().body("原密码错误");
            }

            // 检查新旧密码是否相同
            if (request.getOldPassword().equals(request.getNewPassword())) {
                System.out.println("❌ 新密码不能与原密码相同");
                return ResponseEntity.badRequest().body("新密码不能与原密码相同");
            }

            // 修改密码
            User user = userService.changePassword(id, request.getNewPassword());
            System.out.println("✅ 密码修改成功");
            return ResponseEntity.ok("密码修改成功");
        } catch (RuntimeException e) {
            System.err.println("❌ 修改密码失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    /**
     * 修复管理员状态（管理员权限）
     */
    @PostMapping("/{id}/fix-admin")
    public ResponseEntity<?> fixAdminStatus(@PathVariable Long id,
                                            @RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 修复管理员状态 ===");
            System.out.println("目标用户ID: " + id);

            if (!isAdmin(token)) {
                System.out.println("❌ 需要管理员权限");
                return ResponseEntity.status(403).body("需要管理员权限");
            }

            User user = userService.fixAdminStatus(id);
            System.out.println("✅ 管理员状态修复完成");
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            System.err.println("❌ 修复管理员状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== 修复的工具方法 ====================

    /**
     * 从Token中获取用户ID - 修复版本
     */
    private Long getUserIdFromToken(String token) {
        try {
            String cleanedToken = cleanToken(token);
            String username = jwtUtil.getUsernameFromToken(cleanedToken);
            System.out.println("从Token解析用户名: " + username);

            // 通过用户名查询数据库获取用户ID
            Optional<User> userOptional = userService.getUserByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                System.out.println("找到用户: ID=" + user.getId() + ", 用户名=" + user.getUsername());
                return user.getId();
            } else {
                System.err.println("❌ 用户不存在: " + username);
                throw new RuntimeException("用户不存在: " + username);
            }
        } catch (Exception e) {
            System.err.println("❌ 获取用户ID失败: " + e.getMessage());
            throw new RuntimeException("Token解析失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否是管理员 - 修复版本
     */
    private boolean isAdmin(String token) {
        try {
            String cleanedToken = cleanToken(token);
            String role = jwtUtil.getRoleFromToken(cleanedToken);
            boolean isAdmin = "ADMIN".equals(role);
            System.out.println("Token中的角色: " + role + ", 是否是管理员: " + isAdmin);
            return isAdmin;
        } catch (Exception e) {
            System.err.println("❌ 权限验证失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 清理Token - 修复版本
     */
    private String cleanToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("Token不能为空");
        }
        if (token.startsWith("Bearer ")) {
            return token.substring(7).trim();
        }
        return token.trim();
    }

    // ==================== 请求体类 ====================
// ==================== 新增的请求体类 ====================

    public static class VerifyPasswordRequest {
        private String password;

        // Getters and Setters
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class ChangePasswordVerifyRequest {
        private String oldPassword;
        private String newPassword;

        // Getters and Setters
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
    public static class ApproveRequest {
        private Boolean approved;
        private String reviewComment;

        // Getters and Setters
        public Boolean getApproved() { return approved; }
        public void setApproved(Boolean approved) { this.approved = approved; }
        public String getReviewComment() { return reviewComment; }
        public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
    }

    public static class BatchApproveRequest {
        private List<Long> userIds;
        private Boolean approved;

        // Getters and Setters
        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
        public Boolean getApproved() { return approved; }
        public void setApproved(Boolean approved) { this.approved = approved; }
    }

    public static class RechargeRequest {
        private Double amount;

        // Getters and Setters
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
    }

    public static class ChangePasswordRequest {
        private String newPassword;

        // Getters and Setters
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}
