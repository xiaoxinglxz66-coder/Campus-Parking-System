package com.example.campusparkingbackend.config;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class AlipayConfig {

    private static final Logger logger = LoggerFactory.getLogger(AlipayConfig.class);

    @Value("${alipay.app-id}")
    private String appId;

    @Value("${alipay.merchant-private-key}")
    private String merchantPrivateKey;

    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;

    @Value("${alipay.gateway}")
    private String gateway;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    @Value("${alipay.return-url}")
    private String returnUrl;

    @PostConstruct
    public void init() {
        try {
            Config config = new Config();

            // 使用新的沙箱网关地址
            config.protocol = "https";
            config.gatewayHost = "openapi-sandbox.dl.alipaydev.com";  // 更新这里
            config.signType = "RSA2";

            config.appId = appId;
            config.merchantPrivateKey = formatPrivateKey(merchantPrivateKey);
            config.alipayPublicKey = formatPublicKey(alipayPublicKey);
            config.notifyUrl = notifyUrl;

            Factory.setOptions(config);

            logger.info("✅ 支付宝SDK初始化成功");
            logger.info("AppId: {}", appId);
            logger.info("Gateway: {}", config.gatewayHost);
            logger.info("NotifyUrl: {}", notifyUrl);
            logger.info("ReturnUrl: {}", returnUrl);

            // 测试连接
            testAlipayConnection();

        } catch (Exception e) {
            logger.error("❌ 支付宝SDK初始化失败", e);
            throw new RuntimeException("支付宝配置初始化失败", e);
        }
    }

    /**
     * 测试支付宝连接
     */
    private void testAlipayConnection() {
        try {
            logger.info("🔄 测试支付宝连接...");
            // 简单的配置检查
            if (appId == null || appId.isEmpty()) {
                throw new RuntimeException("AppId 未配置");
            }
            if (merchantPrivateKey == null || merchantPrivateKey.isEmpty()) {
                throw new RuntimeException("商户私钥未配置");
            }
            if (alipayPublicKey == null || alipayPublicKey.isEmpty()) {
                throw new RuntimeException("支付宝公钥未配置");
            }

            logger.info("✅ 支付宝配置检查通过");

        } catch (Exception e) {
            logger.error("❌ 支付宝连接测试失败", e);
            throw new RuntimeException("支付宝连接测试失败: " + e.getMessage(), e);
        }
    }

    /**
     * 格式化私钥
     */
    private String formatPrivateKey(String privateKey) {
        if (privateKey == null) return null;
        return privateKey.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
    }

    /**
     * 格式化公钥
     */
    private String formatPublicKey(String publicKey) {
        if (publicKey == null) return null;
        return publicKey.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }
}
