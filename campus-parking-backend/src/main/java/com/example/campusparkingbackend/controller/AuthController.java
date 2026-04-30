package com.example.campusparkingbackend.controller;

import com.example.campusparkingbackend.dto.AuthRequest;
import com.example.campusparkingbackend.dto.AuthResponse;
import com.example.campusparkingbackend.entity.User;
import com.example.campusparkingbackend.repository.UserRepository;
import com.example.campusparkingbackend.util.JwtUtil;
import com.example.campusparkingbackend.service.SmsCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SmsCodeService smsCodeService;

    // 其他现有方法保持不变...
    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            // 先检查用户是否存在及其状态
            Optional<User> userOptional = userRepository.findByUsername(authRequest.getUsername());
            if (!userOptional.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "用户不存在"));
            }

            User user = userOptional.get();

            // 检查用户审核状态 - 只对校内用户进行审核检查，管理员和校外用户不受限制
            if (user.getUserType() != User.UserType.ADMIN && user.getUserType() != User.UserType.EXTERNAL_USER) {
                // 只有校内用户（STUDENT, TEACHER, STAFF）需要审核
                if (user.getUserStatus() == User.UserStatus.PENDING) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "账号正在审核中，请等待管理员审核"));
                }

                if (user.getUserStatus() == User.UserStatus.REJECTED) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "账号审核未通过，请联系管理员"));
                }
            }

            // 继续原有的认证流程
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String role = authentication.getAuthorities().iterator().next().getAuthority();
            String token = jwtUtil.generateToken(
                    user.getUsername(),
                    user.getUserType().name(),
                    user.getId()  // ✅ 传入用户ID
            );

            AuthResponse response = new AuthResponse(
                    token,
                    authRequest.getUsername(),
                    role,
                    "登录成功"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "用户名或密码错误"));
        }
    }

    // 简化登录也需要同样的修复
    @PostMapping("/login-simple")
    public ResponseEntity<?> loginSimple(@RequestBody AuthRequest authRequest) {
        try {
            System.out.println("=== 简化登录开始 ===");
            System.out.println("接收到的用户名: " + authRequest.getUsername());
            System.out.println("接收到的密码长度: " + (authRequest.getPassword() != null ? authRequest.getPassword().length() : "null"));

            // 手动验证用户
            Optional<User> userOptional = userRepository.findByUsername(authRequest.getUsername());
            if (!userOptional.isPresent()) {
                System.out.println("错误: 用户不存在 - " + authRequest.getUsername());
                return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));
            }

            User user = userOptional.get();

            // 新增：检查用户审核状态 - 只对校内用户进行审核检查
            if (user.getUserType() != User.UserType.ADMIN && user.getUserType() != User.UserType.EXTERNAL_USER) {
                // 只有校内用户（STUDENT, TEACHER, STAFF）需要审核
                if (user.getUserStatus() == User.UserStatus.PENDING) {
                    System.out.println("错误: 用户待审核 - " + authRequest.getUsername());
                    return ResponseEntity.badRequest().body(Map.of("error", "账号正在审核中，请等待管理员审核"));
                }

                if (user.getUserStatus() == User.UserStatus.REJECTED) {
                    System.out.println("错误: 用户审核拒绝 - " + authRequest.getUsername());
                    return ResponseEntity.badRequest().body(Map.of("error", "账号审核未通过，请联系管理员"));
                }
            }

            System.out.println("找到用户: " + user.getUsername());
            System.out.println("用户类型: " + user.getUserType());
            System.out.println("用户状态: " + user.getUserStatus());
            System.out.println("数据库密码哈希: " + user.getPassword());

            boolean passwordValid = passwordEncoder.matches(authRequest.getPassword(), user.getPassword());
            System.out.println("密码验证结果: " + passwordValid);

            if (!passwordValid) {
                System.out.println("错误: 密码不匹配");
                return ResponseEntity.badRequest().body(Map.of("error", "密码错误"));
            }

            // 手动生成token
            String role = user.getUserType().name();
            System.out.println("用户角色: " + role);

            String token = jwtUtil.generateToken(
                    user.getUsername(),
                    user.getUserType().name(),
                    user.getId()  // ✅ 传入用户ID
            );
            System.out.println("生成的Token: " + token);

            System.out.println("=== 简化登录成功 ===");

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", user.getUsername(),
                    "role", role,
                    "message", "简化登录成功"
            ));

        } catch (Exception e) {
            System.out.println("简化登录异常: " + e.getMessage());
            e.printStackTrace(); // 打印完整堆栈
            return ResponseEntity.badRequest().body(Map.of("error", "登录异常: " + e.getMessage()));
        }
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            System.out.println("=== 开始用户注册 ===");
            System.out.println("注册用户名: " + user.getUsername());
            System.out.println("用户类型: " + user.getUserType());
            System.out.println("真实姓名: " + user.getRealName());
            System.out.println("手机号: " + user.getPhone());
            System.out.println("邮箱: " + user.getEmail());

            // 检查用户名是否已存在
            if (userRepository.existsByUsername(user.getUsername())) {
                System.out.println("错误: 用户名已存在");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "用户名已存在"));
            }

            // 加密密码
            String rawPassword = user.getPassword();
            user.setPassword(passwordEncoder.encode(rawPassword));
            System.out.println("密码加密完成");

            // 设置用户状态：校外用户和管理员自动通过，校内用户需要审核
            if (user.getUserType() == User.UserType.EXTERNAL_USER || user.getUserType() == User.UserType.ADMIN) {
                user.setUserStatus(User.UserStatus.APPROVED);
                System.out.println("校外用户/管理员，自动审核通过");
            } else {
                user.setUserStatus(User.UserStatus.PENDING);
                System.out.println("校内用户，状态设置为: PENDING");
            }

            User savedUser = userRepository.save(user);
            System.out.println("用户保存成功，ID: " + savedUser.getId() + ", 状态: " + savedUser.getUserStatus());

            // 根据审核状态返回不同消息
            String message = savedUser.getUserStatus() == User.UserStatus.APPROVED
                    ? "注册成功"
                    : "注册成功，请等待管理员审核";

            System.out.println("返回消息: " + message);

            // 只有审核通过的用户才自动登录
            if (savedUser.getUserStatus() == User.UserStatus.APPROVED) {
                String role = "ROLE_" + savedUser.getUserType().name();
                String token = jwtUtil.generateToken(
                        user.getUsername(),
                        user.getUserType().name(),
                        user.getId()  // ✅ 传入用户ID
                );

                AuthResponse response = new AuthResponse(
                        token,
                        savedUser.getUsername(),
                        role,
                        message
                );
                return ResponseEntity.ok(response);
            } else {
                // 未审核用户只返回成功消息，不返回token
                return ResponseEntity.ok(Map.of(
                        "message", message,
                        "username", savedUser.getUsername(),
                        "status", "PENDING"
                ));
            }

        } catch (Exception e) {
            System.out.println("注册失败异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "注册失败: " + e.getMessage()));
        }
    }

    // 验证token
    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            AuthResponse response = new AuthResponse(
                    token,
                    username,
                    role,
                    "Token有效"
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token无效或已过期"));
        }
    }

    // 添加在 AuthController 类中的其他 @PostMapping 方法旁边
    @PostMapping("/reset-admin")
    public ResponseEntity<?> resetAdminPassword(@RequestBody Map<String, String> request) {
        try {
            String newPassword = request.get("newPassword");
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "新密码不能为空"));
            }

            System.out.println("=== 开始重置管理员密码 ===");

            // 查找管理员用户
            Optional<User> adminOptional = userRepository.findByUsername("admin");
            if (!adminOptional.isPresent()) {
                System.out.println("错误: 管理员用户不存在");
                return ResponseEntity.badRequest().body(Map.of("error", "管理员用户不存在"));
            }

            User admin = adminOptional.get();
            System.out.println("找到管理员: " + admin.getUsername());
            System.out.println("原密码哈希: " + admin.getPassword());
            System.out.println("新密码: " + newPassword);

            // 使用相同的 PasswordEncoder 重新编码密码
            String newPasswordHash = passwordEncoder.encode(newPassword);
            System.out.println("新密码哈希: " + newPasswordHash);

            // 验证新密码是否能匹配
            boolean verifyNewPassword = passwordEncoder.matches(newPassword, newPasswordHash);
            System.out.println("新密码验证结果: " + verifyNewPassword);

            // 更新密码
            admin.setPassword(newPasswordHash);
            User savedAdmin = userRepository.save(admin);

            System.out.println("密码更新成功");
            System.out.println("=== 管理员密码重置完成 ===");

            return ResponseEntity.ok(Map.of(
                    "message", "管理员密码重置成功",
                    "username", "admin",
                    "newPassword", newPassword,
                    "passwordHash", savedAdmin.getPassword(),
                    "verification", verifyNewPassword ? "成功" : "失败"
            ));

        } catch (Exception e) {
            System.out.println("重置密码异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "重置失败: " + e.getMessage()));
        }
    }
    /**
     * 发送短信验证码
     */
    @PostMapping("/sms/send")
    public ResponseEntity<?> sendSmsCode(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");

            if (phone == null || phone.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "手机号不能为空"));
            }

            // 简单的手机号格式验证
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                return ResponseEntity.badRequest().body(Map.of("error", "手机号格式不正确"));
            }

            boolean sendResult = smsCodeService.sendCode(phone);

            if (sendResult) {
                return ResponseEntity.ok(Map.of(
                        "message", "验证码发送成功",
                        "phone", phone
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "验证码发送失败，请稍后重试"));
            }

        } catch (Exception e) {
            System.err.println("发送验证码异常: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "系统错误，请稍后重试"));
        }
    }

    // 在手机号登录方法中也确保校外用户自动审核
    @PostMapping("/sms/login")
    public ResponseEntity<?> smsLogin(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");
            String code = request.get("code");

            if (phone == null || phone.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "手机号不能为空"));
            }

            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "验证码不能为空"));
            }

            // 验证验证码
            boolean codeValid = smsCodeService.verifyCode(phone, code);
            if (!codeValid) {
                return ResponseEntity.badRequest().body(Map.of("error", "验证码错误或已过期"));
            }

            // 查找或创建用户
            Optional<User> userOptional = userRepository.findByPhone(phone);
            User user;

            if (userOptional.isPresent()) {
                // 用户已存在
                user = userOptional.get();
                // 确保校外用户状态是已审核
                if (user.getUserType() == User.UserType.EXTERNAL_USER) {
                    user.setUserStatus(User.UserStatus.APPROVED);
                    user = userRepository.save(user);
                }
            } else {
                // 创建新的访客用户 - 校外用户自动审核通过
                user = new User();
                user.setPhone(phone);
                user.setUsername("guest_" + phone); // 生成唯一用户名
                user.setPassword(passwordEncoder.encode("temp_password_" + System.currentTimeMillis())); // 临时密码
                user.setUserType(User.UserType.EXTERNAL_USER);
                user.setUserStatus(User.UserStatus.APPROVED); // 校外用户自动审核通过
                user.setNeedPayment(true); // 校外用户需要收费
                user.setBalance(0.0); // 初始余额为0

                user = userRepository.save(user);
                System.out.println("创建新的访客用户: " + user.getUsername());
            }

            // 检查用户状态（确保可以登录）
            if (user.getUserStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.badRequest().body(Map.of("error", "账号审核中，请等待管理员审核"));
            }

            // 生成token
            String role = user.getUserType().name();
            String token = jwtUtil.generateToken(
                    user.getUsername(),
                    user.getUserType().name(),
                    user.getId()  // ✅ 传入用户ID
            );

            System.out.println("手机号登录成功 - 手机号: " + phone + ", 用户: " + user.getUsername());

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", user.getUsername(),
                    "role", role,
                    "phone", user.getPhone(),
                    "message", "登录成功",
                    "isNewUser", !userOptional.isPresent() // 是否是新用户
            ));

        } catch (Exception e) {
            System.err.println("手机号登录异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "登录失败: " + e.getMessage()));
        }
    }

    /**
     * 检查手机号是否已注册
     */
    @PostMapping("/sms/check-phone")
    public ResponseEntity<?> checkPhone(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");

            if (phone == null || phone.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "手机号不能为空"));
            }

            boolean exists = userRepository.existsByPhone(phone);

            return ResponseEntity.ok(Map.of(
                    "phone", phone,
                    "exists", exists,
                    "message", exists ? "手机号已注册" : "手机号未注册"
            ));

        } catch (Exception e) {
            System.err.println("检查手机号异常: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "系统错误"));
        }
    }

    /**
     * 发送忘记密码验证码（仅限校内用户）
     */
    @PostMapping("/forgot-password/send-code")
    public ResponseEntity<?> sendForgotPasswordCode(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");

            System.out.println("=== 忘记密码 - 发送验证码 ===");
            System.out.println("手机号: " + phone);

            if (phone == null || phone.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "手机号不能为空"
                ));
            }

            // 验证手机号格式
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "手机号格式不正确"
                ));
            }

            // 查找用户
            Optional<User> userOptional = userRepository.findByPhone(phone);

            // 安全策略：无论用户是否存在，都返回相同信息
            if (userOptional.isEmpty()) {
                System.out.println("忘记密码请求 - 手机号未注册: " + phone);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "如果该手机号已注册，验证码将发送到您的手机",
                        "userExists", false
                ));
            }

            User user = userOptional.get();

            // 验证用户类型 - 仅限校内用户
            if (!isCampusUser(user.getUserType())) {
                System.out.println("忘记密码请求 - 非校内用户类型: " + phone + ", 类型: " + user.getUserType());
                // 出于安全考虑，返回相同信息
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "如果该手机号已注册，验证码将发送到您的手机",
                        "userExists", false
                ));
            }

            // 验证用户状态
            if (user.getUserStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "账号状态异常，请联系管理员"
                ));
            }

            // 发送验证码（复用现有SmsCodeService）
            boolean sendResult = smsCodeService.sendCode(phone);

            if (sendResult) {
                System.out.println("验证码发送成功 - 手机号: " + phone + ", 用户: " + user.getUsername());
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "验证码已发送到您的手机",
                        "userExists", true,
                        "maskedPhone", maskPhone(phone)
                ));
            } else {
                return ResponseEntity.status(429).body(Map.of(
                        "success", false,
                        "message", "发送过于频繁，请60秒后重试"
                ));
            }

        } catch (Exception e) {
            System.err.println("发送忘记密码验证码异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误，请稍后重试"
            ));
        }
    }

    /**
     * 通过验证码重置密码（仅限校内用户）
     */
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPasswordWithCode(@RequestBody ForgotPasswordResetRequest request) {
        try {
            System.out.println("=== 忘记密码 - 重置密码 ===");
            System.out.println("手机号: " + request.getPhone());
            System.out.println("验证码: " + request.getCode());

            // 基础验证
            if (request.getPhone() == null || request.getCode() == null || request.getNewPassword() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "参数不完整"
                ));
            }

            // ✅ 新方案：检查验证码是否存在且未过期，但不标记为已使用
            if (!smsCodeService.checkCodeWithoutMarkingUsed(request.getPhone(), request.getCode())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "验证码无效或已过期"
                ));
            }

            // 查找用户
            Optional<User> userOptional = userRepository.findByPhone(request.getPhone());
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "用户不存在"
                ));
            }

            User user = userOptional.get();

            // 验证用户类型 - 仅限校内用户
            if (!isCampusUser(user.getUserType())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "该功能仅限校内用户使用"
                ));
            }

            // 验证用户状态
            if (user.getUserStatus() != User.UserStatus.APPROVED) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "账号状态异常，请联系管理员"
                ));
            }

            // 验证新密码
            if (request.getNewPassword().length() < 6) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "密码长度至少6位"
                ));
            }

            // 检查新旧密码是否相同
            if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "新密码不能与原密码相同"
                ));
            }

            // 更新密码
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            user.setPassword(encodedPassword);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            // 使验证码失效
            smsCodeService.invalidateCode(request.getPhone());

            System.out.println("密码重置成功 - 用户: " + user.getUsername() + ", 手机号: " + request.getPhone());

            // 记录管理员通知
            sendAdminNotification(user, "用户通过忘记密码功能重置了密码");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "密码重置成功，请使用新密码登录",
                    "username", user.getUsername()
            ));

        } catch (Exception e) {
            System.err.println("重置密码异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误，请稍后重试"
            ));
        }
    }

    /**
     * 判断是否为校内用户
     */
    private boolean isCampusUser(User.UserType userType) {
        return userType == User.UserType.STUDENT ||
                userType == User.UserType.TEACHER ||
                userType == User.UserType.STAFF;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    private void sendAdminNotification(User user, String message) {
        System.out.println("管理员通知: " + message + " - 用户: " + user.getUsername() + " (" + user.getId() + ")");
    }

    /**
     * 请求体类
     */
    public static class ForgotPasswordResetRequest {
        private String phone;
        private String code;
        private String newPassword;

        // Getters and Setters
        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}
