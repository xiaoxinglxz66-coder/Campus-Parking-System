package com.example.campusparkingbackend.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class SmsCodeService {

    // 存储验证码，key: 手机号, value: 验证码信息
    private final Map<String, SmsCodeInfo> smsCodeStore = new ConcurrentHashMap<>();

    // 验证码有效期（分钟）
    private static final int CODE_EXPIRE_MINUTES = 5;

    // 发送间隔（秒）
    private static final int SEND_INTERVAL_SECONDS = 60;

    /**
     * 生成6位数字验证码
     */
    public String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
    /**
     * 使某个手机号的验证码失效
     */
    public void invalidateCode(String phone) {
        smsCodeStore.remove(phone);
        System.out.println("清理验证码记录 - 手机号: " + phone);
    }
    /**
     * 发送验证码（模拟）
     */
    public boolean sendCode(String phone) {
        try {
            // 检查发送频率
            if (smsCodeStore.containsKey(phone)) {
                SmsCodeInfo existing = smsCodeStore.get(phone);
                long secondsSinceLastSend = ChronoUnit.SECONDS.between(existing.getSendTime(), LocalDateTime.now());
                if (secondsSinceLastSend < SEND_INTERVAL_SECONDS) {
                    System.out.println("发送过于频繁，请 " + (SEND_INTERVAL_SECONDS - secondsSinceLastSend) + " 秒后重试");
                    return false;
                }
            }

            // 生成验证码
            String code = generateCode();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expireTime = now.plusMinutes(CODE_EXPIRE_MINUTES);

            // 存储验证码信息
            SmsCodeInfo smsCodeInfo = new SmsCodeInfo(code, now, expireTime, false);
            smsCodeStore.put(phone, smsCodeInfo);

            // 模拟发送短信 - 在控制台输出
            System.out.println("==========================================");
            System.out.println("📱 模拟短信发送");
            System.out.println("接收手机: " + phone);
            System.out.println("验证码: " + code);
            System.out.println("有效期: " + CODE_EXPIRE_MINUTES + "分钟");
            System.out.println("==========================================");

            return true;

        } catch (Exception e) {
            System.err.println("发送验证码失败: " + e.getMessage());
            return false;
        }
    }
    /**
     * 检查验证码但不标记为已使用（用于忘记密码流程）
     */
    public boolean checkCodeWithoutMarkingUsed(String phone, String code) {
        try {
            if (!smsCodeStore.containsKey(phone)) {
                System.out.println("未找到该手机号的验证码记录");
                return false;
            }

            SmsCodeInfo smsCodeInfo = smsCodeStore.get(phone);

            // 检查是否已过期
            if (LocalDateTime.now().isAfter(smsCodeInfo.getExpireTime())) {
                System.out.println("验证码已过期");
                smsCodeStore.remove(phone); // 清理过期验证码
                return false;
            }

            // 检查验证码是否正确
            if (!smsCodeInfo.getCode().equals(code)) {
                System.out.println("验证码不正确");
                return false;
            }

            // ✅ 注意：这里不标记为已使用
            System.out.println("验证码检查通过（未标记为已使用）- 手机号: " + phone);
            return true;

        } catch (Exception e) {
            System.err.println("检查验证码失败: " + e.getMessage());
            return false;
        }
    }
    /**
     * 验证验证码
     */
    public boolean verifyCode(String phone, String code) {
        try {
            if (!smsCodeStore.containsKey(phone)) {
                System.out.println("未找到该手机号的验证码记录");
                return false;
            }

            SmsCodeInfo smsCodeInfo = smsCodeStore.get(phone);

            // 检查是否已使用
            if (smsCodeInfo.isUsed()) {
                System.out.println("验证码已被使用");
                return false;
            }

            // 检查是否过期
            if (LocalDateTime.now().isAfter(smsCodeInfo.getExpireTime())) {
                System.out.println("验证码已过期");
                smsCodeStore.remove(phone);
                return false;
            }

            // 检查验证码是否正确
            if (!smsCodeInfo.getCode().equals(code)) {
                System.out.println("验证码不正确");
                return false;
            }

            // 标记为已使用
            smsCodeInfo.setUsed(true);
            smsCodeStore.put(phone, smsCodeInfo);

            System.out.println("验证码验证成功 - 手机号: " + phone);
            return true;

        } catch (Exception e) {
            System.err.println("验证验证码失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 清理过期的验证码
     */
    public void cleanExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        smsCodeStore.entrySet().removeIf(entry ->
                now.isAfter(entry.getValue().getExpireTime())
        );
    }

    /**
     * 验证码信息内部类
     */
    public static class SmsCodeInfo {
        private String code;
        private LocalDateTime sendTime;
        private LocalDateTime expireTime;
        private boolean used;

        public SmsCodeInfo(String code, LocalDateTime sendTime, LocalDateTime expireTime, boolean used) {
            this.code = code;
            this.sendTime = sendTime;
            this.expireTime = expireTime;
            this.used = used;
        }

        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public LocalDateTime getSendTime() { return sendTime; }
        public void setSendTime(LocalDateTime sendTime) { this.sendTime = sendTime; }

        public LocalDateTime getExpireTime() { return expireTime; }
        public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }

        public boolean isUsed() { return used; }
        public void setUsed(boolean used) { this.used = used; }
    }
}
