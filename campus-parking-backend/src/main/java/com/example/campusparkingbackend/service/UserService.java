// src/main/java/com/example/campusparkingbackend/service/UserService.java
package com.example.campusparkingbackend.service;

import com.example.campusparkingbackend.entity.ParkingRecord;
import com.example.campusparkingbackend.entity.User;
import com.example.campusparkingbackend.entity.Vehicle;
import com.example.campusparkingbackend.repository.ParkingRecordRepository;
import com.example.campusparkingbackend.repository.UserRepository;
import com.example.campusparkingbackend.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ParkingRecordRepository parkingRecordRepository;
    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 根据ID获取用户
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * 根据用户名获取用户
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    /**
     * 重置用户密码
     */
    public User resetUserPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查是否为当前用户（可选，防止管理员重置自己的密码）
        // 如果需要可以在这里添加检查逻辑

        // 重置密码为默认密码 "123456"
        user.setPassword(passwordEncoder.encode("123456"));
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
    public Long getUserIdByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return user.getId();
    }
// 在 UserService.java 中添加以下方法：

    /**
     * 验证用户密码（用于修改密码前的验证）
     */
    public boolean verifyPassword(Long userId, String password) {
        try {
            System.out.println("=== 验证用户密码 ===");
            System.out.println("用户ID: " + userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));

            boolean isValid = passwordEncoder.matches(password, user.getPassword());
            System.out.println("密码验证结果: " + isValid);

            return isValid;
        } catch (Exception e) {
            System.err.println("❌ 验证密码失败: " + e.getMessage());
            throw new RuntimeException("验证密码失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码（需要验证原密码）- 新版本
     */
    @Transactional
    public User changePasswordWithVerify(Long userId, String oldPassword, String newPassword) {
        try {
            System.out.println("=== 修改密码（需要验证原密码） ===");
            System.out.println("用户ID: " + userId);

            // 验证原密码
            if (!verifyPassword(userId, oldPassword)) {
                throw new RuntimeException("原密码错误");
            }

            // 检查新旧密码是否相同
            if (oldPassword.equals(newPassword)) {
                throw new RuntimeException("新密码不能与原密码相同");
            }

            // 验证新密码长度
            if (newPassword == null || newPassword.length() < 6) {
                throw new RuntimeException("密码至少6位");
            }

            // 修改密码
            User user = changePassword(userId, newPassword);
            System.out.println("✅ 密码修改成功");

            return user;
        } catch (RuntimeException e) {
            System.err.println("❌ 修改密码失败: " + e.getMessage());
            throw e;
        }
    }

// 保持现有的 changePassword 方法不变，用于管理员重置密码
// @Transactional
// public User changePassword(Long userId, String newPassword) {
//     // 现有的实现...
// }
    /**
     * 重置管理员密码（临时管理功能）
     */
    @Transactional
    public User resetAdminPassword(Long adminId, String newPassword) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员用户不存在，ID: " + adminId));

        // 验证用户类型是管理员
        if (admin.getUserType() != User.UserType.ADMIN) {
            throw new RuntimeException("该用户不是管理员");
        }

        admin.setPassword(passwordEncoder.encode(newPassword));
        admin.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(admin);
    }

    /**
     * 重置指定用户的密码（管理员权限）
     */
    @Transactional
    public User resetUserPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * 创建新的管理员账户
     */
    @Transactional
    public User createAdminUser(String username, String password, String realName, String phone, String email) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在: " + username);
        }

        User admin = new User();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRealName(realName);
        admin.setPhone(phone);
        admin.setEmail(email);
        admin.setUserType(User.UserType.ADMIN);
        admin.setUserStatus(User.UserStatus.APPROVED);
        admin.setBalance(0.0);
        admin.setNeedPayment(false);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(admin);
    }

    /**
     * 获取所有管理员账户
     */
    public List<User> getAllAdminUsers() {
        return userRepository.findByUserType(User.UserType.ADMIN);
    }

    /**
     * 创建用户
     */
    @Transactional
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在: " + user.getUsername());
        }

        // 检查手机号是否已存在
        if (user.getPhone() != null && userRepository.existsByPhone(user.getPhone())) {
            throw new RuntimeException("手机号已存在: " + user.getPhone());
        }

        // 检查邮箱是否已存在
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在: " + user.getEmail());
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置时间戳 - 修复这里：使用 setUpdatedAt 而不是 setUpdatedTime
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + id));

        // 如果修改了手机号，检查是否重复
        if (userDetails.getPhone() != null &&
                !userDetails.getPhone().equals(user.getPhone()) &&
                userRepository.existsByPhone(userDetails.getPhone())) {
            throw new RuntimeException("手机号已存在: " + userDetails.getPhone());
        }

        // 如果修改了邮箱，检查是否重复
        if (userDetails.getEmail() != null &&
                !userDetails.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(userDetails.getEmail())) {
            throw new RuntimeException("邮箱已存在: " + userDetails.getEmail());
        }

        // 更新字段
        if (userDetails.getRealName() != null) {
            user.setRealName(userDetails.getRealName());
        }
        if (userDetails.getPhone() != null) {
            user.setPhone(userDetails.getPhone());
        }
        if (userDetails.getEmail() != null) {
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getUserType() != null) {
            user.setUserType(userDetails.getUserType());
        }
        if (userDetails.getBalance() != null) {
            user.setBalance(userDetails.getBalance());
        }
        if (userDetails.getNeedPayment() != null) {
            user.setNeedPayment(userDetails.getNeedPayment());
        }

        // 修复这里：使用 setUpdatedAt 而不是 setUpdatedTime
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long userId, Long currentUserId) {
        try {
            System.out.println("=== 删除用户（修复版本） ===");
            System.out.println("目标用户ID: " + userId);
            System.out.println("当前操作用户ID: " + currentUserId);

            // 检查用户是否存在
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));

            // 不能删除自己
            if (userId.equals(currentUserId)) {
                throw new RuntimeException("不能删除自己的账户");
            }

            // 检查用户是否有正在停车的记录
            boolean hasActiveParking = hasActiveParkingRecords(userId);
            if (hasActiveParking) {
                throw new RuntimeException("用户有正在停车的记录，无法删除");
            }

            System.out.println("✅ 开始删除用户关联数据...");

            // 1. 先删除用户的停车记录
            System.out.println("🔧 删除停车记录...");
            List<ParkingRecord> userParkingRecords = parkingRecordRepository.findByUserId(userId);
            if (!userParkingRecords.isEmpty()) {
                System.out.println("  删除 " + userParkingRecords.size() + " 条停车记录");
                parkingRecordRepository.deleteAll(userParkingRecords);
                parkingRecordRepository.flush(); // 立即执行删除
            }

            // 2. 删除用户的车辆
            System.out.println("🔧 删除车辆...");
            List<Vehicle> userVehicles = vehicleRepository.findByUserId(userId);
            if (!userVehicles.isEmpty()) {
                System.out.println("  删除 " + userVehicles.size() + " 辆车辆");
                vehicleRepository.deleteAll(userVehicles);
                vehicleRepository.flush(); // 立即执行删除
            }

            // 3. 最后删除用户
            System.out.println("🔧 删除用户...");
            userRepository.delete(user);
            userRepository.flush(); // 立即执行删除

            System.out.println("✅ 用户删除成功，ID: " + userId);

        } catch (RuntimeException e) {
            System.out.println("❌ 删除用户失败: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("❌ 删除用户异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("删除用户失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户是否有正在停车的记录
     */
    private boolean hasActiveParkingRecords(Long userId) {
        try {
            System.out.println("🔍 检查用户 " + userId + " 的停车记录状态");

            // 方法1: 查询进行中的停车记录
            List<ParkingRecord> activeRecords = parkingRecordRepository.findByUserIdAndStatus(userId, ParkingRecord.ParkingStatus.PARKING);
            boolean hasActive = !activeRecords.isEmpty();

            if (hasActive) {
                System.out.println("⚠️ 用户有 " + activeRecords.size() + " 条进行中的停车记录");
                for (ParkingRecord record : activeRecords) {
                    System.out.println("  记录ID: " + record.getId() + ", 车牌: " +
                            (record.getVehicle() != null ? record.getVehicle().getPlateNumber() : "未知"));
                }
            } else {
                System.out.println("✅ 用户没有进行中的停车记录");
            }

            return hasActive;

        } catch (Exception e) {
            System.out.println("❌ 检查停车记录异常: " + e.getMessage());
            // 如果检查失败，保守起见不允许删除
            return true;
        }
    }

    /**
     * 审核用户
     */
    @Transactional
    public User approveUser(Long userId, Boolean approved, String reviewComment) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));

        if (approved) {
            user.setUserStatus(User.UserStatus.APPROVED);
        } else {
            user.setUserStatus(User.UserStatus.REJECTED);
        }

        // 修复这里：使用 setUpdatedAt 而不是 setUpdatedTime
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * 更新用户余额
     */
    @Transactional
    public User updateUserBalance(Long userId, Double amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));

        Double newBalance = user.getBalance() + amount;
        if (newBalance < 0) {
            throw new RuntimeException("余额不足");
        }

        user.setBalance(newBalance);

        // 修复这里：使用 setUpdatedAt 而不是 setUpdatedTime
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * 充值
     */
    @Transactional
    public User recharge(Long userId, Double amount) {
        if (amount <= 0) {
            throw new RuntimeException("充值金额必须大于0");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));

        user.setBalance(user.getBalance() + amount);

        // 修复这里：使用 setUpdatedAt 而不是 setUpdatedTime
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * 消费
     */
    @Transactional
    public User consume(Long userId, Double amount) {
        if (amount <= 0) {
            throw new RuntimeException("消费金额必须大于0");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));

        if (user.getBalance() < amount) {
            throw new RuntimeException("余额不足");
        }

        user.setBalance(user.getBalance() - amount);

        // 修复这里：使用 setUpdatedAt 而不是 setUpdatedTime
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * 修改密码
     */
    @Transactional
    public User changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));

        user.setPassword(passwordEncoder.encode(newPassword));

        // 修复这里：使用 setUpdatedAt 而不是 setUpdatedTime
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
    // 在 UserService.java 中添加这些方法：

    /**
     * 批量审核用户
     */
    @Transactional
    public List<User> batchApproveUsers(List<Long> userIds, Boolean approved) {
        List<User> users = userRepository.findAllById(userIds);

        for (User user : users) {
            if (approved) {
                user.setUserStatus(User.UserStatus.APPROVED);
            } else {
                user.setUserStatus(User.UserStatus.REJECTED);
            }
            user.setUpdatedAt(LocalDateTime.now());
        }

        return userRepository.saveAll(users);
    }

    /**
     * 根据状态获取用户
     */
    public List<User> getUsersByStatus(User.UserStatus status) {
        return userRepository.findByUserStatus(status);
    }



    /**
     * 获取用户详细信息（包含关联数据）
     */
    public User getUserWithDetails(Long userId) {
        System.out.println("获取用户详细信息，ID: " + userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));
        // 这里可以根据需要加载关联的车辆、停车记录等信息
    }



    /**
     * 修复管理员状态（确保管理员账号状态正确）
     */
    @Transactional
    public User fixAdminStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));

        // 如果是管理员，确保状态为已审核
        if (user.getUserType() == User.UserType.ADMIN) {
            user.setUserStatus(User.UserStatus.APPROVED);
            user.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }

        return user;
    }

    /**
     * 获取用户统计信息（使用正确的类名）
     */
    public UserStatistics getUserStats() {
        return getUserStatistics(); // 调用现有的方法
    }

    /**
     * 获取待审核用户
     */
    public List<User> getPendingUsers() {
        return userRepository.findPendingUsers();
    }

    /**
     * 获取已审核用户
     */
    public List<User> getApprovedUsers() {
        return userRepository.findApprovedUsers();
    }

    /**
     * 根据用户类型获取用户
     */
    public List<User> getUsersByType(User.UserType userType) {
        return userRepository.findByUserType(userType);
    }

    /**
     * 统计用户数量
     */
    public long getUserCount() {
        return userRepository.count();
    }

    /**
     * 统计各类用户数量
     */
    public UserStatistics getUserStatistics() {
        long totalUsers = userRepository.count();
        long pendingUsers = userRepository.countByUserStatus(User.UserStatus.PENDING);
        long approvedUsers = userRepository.countByUserStatus(User.UserStatus.APPROVED);
        long rejectedUsers = userRepository.countByUserStatus(User.UserStatus.REJECTED);

        return new UserStatistics(totalUsers, pendingUsers, approvedUsers, rejectedUsers);
    }

    /**
     * 根据类型获取待审核用户
     */
    public List<User> getPendingUsersByType(User.UserType userType) {
        return userRepository.findByUserTypeAndUserStatus(userType, User.UserStatus.PENDING);
    }

    /**
     * 用户统计信息类
     */
    public static class UserStatistics {
        private final long totalUsers;
        private final long pendingUsers;
        private final long approvedUsers;
        private final long rejectedUsers;

        public UserStatistics(long totalUsers, long pendingUsers, long approvedUsers, long rejectedUsers) {
            this.totalUsers = totalUsers;
            this.pendingUsers = pendingUsers;
            this.approvedUsers = approvedUsers;
            this.rejectedUsers = rejectedUsers;
        }

        // Getters
        public long getTotalUsers() { return totalUsers; }
        public long getPendingUsers() { return pendingUsers; }
        public long getApprovedUsers() { return approvedUsers; }
        public long getRejectedUsers() { return rejectedUsers; }
    }
}
