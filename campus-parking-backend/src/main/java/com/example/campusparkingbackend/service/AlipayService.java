package com.example.campusparkingbackend.service;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AlipayService {

    private static final Logger logger = LoggerFactory.getLogger(AlipayService.class);

    /**
     * 创建电脑网站支付
     */
    public AlipayTradePagePayResponse createPagePay(String outTradeNo, String totalAmount,
                                                    String subject, String returnUrl) throws Exception {
        logger.info("🔄 创建支付宝支付订单:");
        logger.info("  订单号: {}", outTradeNo);
        logger.info("  金额: {}", totalAmount);
        logger.info("  主题: {}", subject);
        logger.info("  返回URL: {}", returnUrl);

        try {
            AlipayTradePagePayResponse response = Factory.Payment
                    .Page()
                    .pay(subject, outTradeNo, totalAmount, returnUrl);

            logger.info("✅ 支付宝支付订单创建成功");
            logger.info("  订单号: {}", outTradeNo);

            return response;

        } catch (Exception e) {
            logger.error("❌ 创建支付宝支付订单失败", e);
            throw new RuntimeException("创建支付订单失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询订单状态
     */
    public String queryOrder(String outTradeNo) throws Exception {
        try {
            return Factory.Payment
                    .Common()
                    .query(outTradeNo)
                    .getHttpBody();
        } catch (Exception e) {
            logger.error("❌ 查询订单状态失败: {}", outTradeNo, e);
            throw new RuntimeException("查询订单状态失败: " + e.getMessage(), e);
        }
    }

    /**
     * 退款
     */
    public String refund(String outTradeNo, String refundAmount) throws Exception {
        try {
            return Factory.Payment
                    .Common()
                    .refund(outTradeNo, refundAmount)
                    .getHttpBody();
        } catch (Exception e) {
            logger.error("❌ 退款失败: {}", outTradeNo, e);
            throw new RuntimeException("退款失败: " + e.getMessage(), e);
        }
    }
}
