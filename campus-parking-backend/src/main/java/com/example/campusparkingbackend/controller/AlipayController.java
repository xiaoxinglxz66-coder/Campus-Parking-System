package com.example.campusparkingbackend.controller;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.example.campusparkingbackend.service.AlipayService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/alipay")
@CrossOrigin
public class AlipayController {

    private static final Logger logger = LoggerFactory.getLogger(AlipayController.class);

    @Autowired
    private AlipayService alipayService;

    /**
     * 创建支付宝支付订单
     */
    @PostMapping("/create")
    public Map<String, Object> createPayment(@RequestBody Map<String, Object> params,
                                             HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            String outTradeNo = "PARK" + System.currentTimeMillis();
            Double amount = Double.valueOf(params.get("amount").toString());
            String subject = params.get("subject").toString();
            String recordId = params.get("recordId") != null ? params.get("recordId").toString() : "";

            // 构建返回URL - 使用前端地址
            String returnUrl = "http://localhost:3000/external-user?recordId=" + recordId;

            logger.info("💰 创建支付宝支付订单:");
            logger.info("  订单号: {}", outTradeNo);
            logger.info("  金额: {}", amount);
            logger.info("  主题: {}", subject);
            logger.info("  返回URL: {}", returnUrl);

            // 调用支付宝创建支付
            AlipayTradePagePayResponse response = alipayService.createPagePay(
                    outTradeNo,
                    String.format("%.2f", amount),
                    subject,
                    returnUrl
            );

            // AlipayTradePagePayResponse 没有 getHttpStatusCode() 方法
            // 直接检查响应体是否为空
            if (response != null && response.getBody() != null && !response.getBody().isEmpty()) {
                logger.info("✅ 支付宝返回表单数据，长度: {}", response.getBody().length());
                logger.debug("支付宝返回的表单内容: {}", response.getBody());

                result.put("success", true);
                result.put("outTradeNo", outTradeNo);
                result.put("form", response.getBody());
                result.put("message", "支付订单创建成功");
            } else {
                logger.error("❌ 支付宝返回空响应");
                result.put("success", false);
                result.put("message", "支付宝服务返回空响应");
            }

        } catch (Exception e) {
            logger.error("❌ 创建支付订单异常", e);
            result.put("success", false);
            result.put("message", "支付订单创建失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 测试支付页面（模拟支付宝支付）
     */
    @PostMapping("/test-payment")
    public ResponseEntity<String> testPayment(@RequestBody Map<String, Object> params,
                                              HttpServletRequest request) {
        try {
            String outTradeNo = "TEST" + System.currentTimeMillis();
            Double amount = Double.valueOf(params.get("amount").toString());
            String subject = params.get("subject").toString();
            String recordId = params.get("recordId") != null ? params.get("recordId").toString() : "";

            // 构建完整的测试支付页面
            String baseUrl = request.getScheme() + "://" + request.getServerName() +
                    (request.getServerPort() == 80 ? "" : ":" + request.getServerPort());

            String html = "<!DOCTYPE html>\n" +
                    "<html lang=\"zh-CN\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>校园停车费支付 - 测试页面</title>\n" +
                    "    <style>\n" +
                    "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                    "        body { \n" +
                    "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; \n" +
                    "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                    "            min-height: 100vh; \n" +
                    "            display: flex; \n" +
                    "            align-items: center; \n" +
                    "            justify-content: center;\n" +
                    "            padding: 20px;\n" +
                    "        }\n" +
                    "        .payment-container {\n" +
                    "            background: white;\n" +
                    "            border-radius: 15px;\n" +
                    "            padding: 40px;\n" +
                    "            box-shadow: 0 15px 35px rgba(0,0,0,0.1);\n" +
                    "            max-width: 500px;\n" +
                    "            width: 100%;\n" +
                    "        }\n" +
                    "        .header {\n" +
                    "            text-align: center;\n" +
                    "            margin-bottom: 30px;\n" +
                    "        }\n" +
                    "        .header h1 {\n" +
                    "            color: #1890ff;\n" +
                    "            font-size: 28px;\n" +
                    "            margin-bottom: 10px;\n" +
                    "        }\n" +
                    "        .header p {\n" +
                    "            color: #666;\n" +
                    "            font-size: 16px;\n" +
                    "        }\n" +
                    "        .payment-info {\n" +
                    "            background: #f8f9fa;\n" +
                    "            border-radius: 10px;\n" +
                    "            padding: 20px;\n" +
                    "            margin-bottom: 30px;\n" +
                    "        }\n" +
                    "        .info-item {\n" +
                    "            display: flex;\n" +
                    "            justify-content: space-between;\n" +
                    "            margin-bottom: 10px;\n" +
                    "            padding: 8px 0;\n" +
                    "        }\n" +
                    "        .info-item:last-child {\n" +
                    "            margin-bottom: 0;\n" +
                    "        }\n" +
                    "        .label {\n" +
                    "            color: #666;\n" +
                    "            font-weight: 500;\n" +
                    "        }\n" +
                    "        .value {\n" +
                    "            color: #333;\n" +
                    "            font-weight: 600;\n" +
                    "        }\n" +
                    "        .amount {\n" +
                    "            font-size: 32px;\n" +
                    "            color: #ff4d4f;\n" +
                    "            text-align: center;\n" +
                    "            margin: 20px 0;\n" +
                    "            font-weight: bold;\n" +
                    "        }\n" +
                    "        .payment-buttons {\n" +
                    "            display: flex;\n" +
                    "            gap: 15px;\n" +
                    "            flex-direction: column;\n" +
                    "        }\n" +
                    "        .btn {\n" +
                    "            padding: 15px 30px;\n" +
                    "            border: none;\n" +
                    "            border-radius: 8px;\n" +
                    "            font-size: 16px;\n" +
                    "            font-weight: 600;\n" +
                    "            cursor: pointer;\n" +
                    "            transition: all 0.3s ease;\n" +
                    "            text-align: center;\n" +
                    "        }\n" +
                    "        .btn-success {\n" +
                    "            background: #52c41a;\n" +
                    "            color: white;\n" +
                    "        }\n" +
                    "        .btn-success:hover {\n" +
                    "            background: #389e0d;\n" +
                    "            transform: translateY(-2px);\n" +
                    "        }\n" +
                    "        .btn-danger {\n" +
                    "            background: #ff4d4f;\n" +
                    "            color: white;\n" +
                    "        }\n" +
                    "        .btn-danger:hover {\n" +
                    "            background: #d9363e;\n" +
                    "            transform: translateY(-2px);\n" +
                    "        }\n" +
                    "        .btn-secondary {\n" +
                    "            background: #f0f0f0;\n" +
                    "            color: #666;\n" +
                    "        }\n" +
                    "        .btn-secondary:hover {\n" +
                    "            background: #d9d9d9;\n" +
                    "        }\n" +
                    "        .note {\n" +
                    "            text-align: center;\n" +
                    "            color: #999;\n" +
                    "            font-size: 12px;\n" +
                    "            margin-top: 20px;\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"payment-container\">\n" +
                    "        <div class=\"header\">\n" +
                    "            <h1>🚗 校园停车费支付</h1>\n" +
                    "            <p>测试支付页面 - 模拟支付宝支付</p>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"payment-info\">\n" +
                    "            <div class=\"info-item\">\n" +
                    "                <span class=\"label\">订单号：</span>\n" +
                    "                <span class=\"value\">" + outTradeNo + "</span>\n" +
                    "            </div>\n" +
                    "            <div class=\"info-item\">\n" +
                    "                <span class=\"label\">停车记录：</span>\n" +
                    "                <span class=\"value\">#" + recordId + "</span>\n" +
                    "            </div>\n" +
                    "            <div class=\"info-item\">\n" +
                    "                <span class=\"label\">项目：</span>\n" +
                    "                <span class=\"value\">" + subject + "</span>\n" +
                    "            </div>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"amount\">¥" + amount + "</div>\n" +
                    "        \n" +
                    "        <div class=\"payment-buttons\">\n" +
                    "            <button class=\"btn btn-success\" onclick=\"simulatePayment('success')\">\n" +
                    "                💰 模拟支付成功\n" +
                    "            </button>\n" +
                    "            <button class=\"btn btn-danger\" onclick=\"simulatePayment('fail')\">\n" +
                    "                ❌ 模拟支付失败\n" +
                    "            </button>\n" +
                    "            <button class=\"btn btn-secondary\" onclick=\"closeWindow()\">\n" +
                    "                🔙 返回\n" +
                    "            </button>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"note\">\n" +
                    "            注意：这是测试支付页面，实际环境中将跳转到支付宝支付\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "\n" +
                    "    <script>\n" +
                    "        function simulatePayment(status) {\n" +
                    "            if (status === 'success') {\n" +
                    "                alert('支付成功！停车记录已完成。');\n" +
                    "                // 通知父窗口支付成功\n" +
                    "                if (window.opener) {\n" +
                    "                    window.opener.postMessage({ \n" +
                    "                        type: 'PAYMENT_SUCCESS', \n" +
                    "                        recordId: '" + recordId + "' \n" +
                    "                    }, '*');\n" +
                    "                }\n" +
                    "            } else {\n" +
                    "                alert('支付失败！请重试或联系管理员。');\n" +
                    "            }\n" +
                    "            window.close();\n" +
                    "        }\n" +
                    "        \n" +
                    "        function closeWindow() {\n" +
                    "            window.close();\n" +
                    "        }\n" +
                    "        \n" +
                    "        // 监听键盘事件\n" +
                    "        document.addEventListener('keydown', function(e) {\n" +
                    "            if (e.key === 'Escape') {\n" +
                    "                closeWindow();\n" +
                    "            }\n" +
                    "        });\n" +
                    "    </script>\n" +
                    "</body>\n" +
                    "</html>";

            return ResponseEntity.ok(html);
        } catch (Exception e) {
            String errorHtml = "<html><body><h2>支付页面加载失败</h2><p>" + e.getMessage() + "</p></body></html>";
            return ResponseEntity.badRequest().body(errorHtml);
        }
    }

    /**
     * 检查支付状态
     */
    @GetMapping("/check-payment/{outTradeNo}")
    public Map<String, Object> checkPayment(@PathVariable String outTradeNo) {
        Map<String, Object> result = new HashMap<>();

        try {
            String queryResult = alipayService.queryOrder(outTradeNo);
            result.put("success", true);
            result.put("data", queryResult);

            // 解析查询结果，判断支付状态
            if (queryResult.contains("\"trade_status\":\"TRADE_SUCCESS\"")) {
                result.put("paid", true);
            } else {
                result.put("paid", false);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/query/{outTradeNo}")
    public Map<String, Object> queryPayment(@PathVariable String outTradeNo) {
        Map<String, Object> result = new HashMap<>();

        try {
            String queryResult = alipayService.queryOrder(outTradeNo);
            result.put("success", true);
            result.put("data", queryResult);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 支付宝异步通知接口
     */
    @PostMapping("/notify")
    public String handleNotify(HttpServletRequest request) {
        try {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();

            for (String name : requestParams.keySet()) {
                String[] values = requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }

            logger.info("📢 收到支付宝异步通知: {}", params);

            String tradeStatus = params.get("trade_status");
            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");

            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                // 支付成功，更新订单状态
                logger.info("✅ 订单支付成功: {}, 支付宝交易号: {}", outTradeNo, tradeNo);

                // TODO: 更新停车记录状态
                // parkingRecordService.updatePaymentStatus(outTradeNo, tradeNo);

                return "success";
            }

        } catch (Exception e) {
            logger.error("❌ 处理支付宝通知异常", e);
        }

        return "failure";
    }

    /**
     * 支付同步返回接口
     */
    @GetMapping("/return")
    public String handleReturn(HttpServletRequest request) {
        try {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();

            for (String name : requestParams.keySet()) {
                String[] values = requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }

            logger.info("🔄 支付宝同步返回: {}", params);

            String html = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <title>支付完成</title>\n" +
                    "    <style>\n" +
                    "        body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }\n" +
                    "        .success { color: #52c41a; font-size: 24px; }\n" +
                    "        .info { margin: 20px 0; }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"success\">✅ 支付处理完成</div>\n" +
                    "    <div class=\"info\">请返回应用查看支付结果</div>\n" +
                    "    <button onclick=\"closeWindow()\">关闭窗口</button>\n" +
                    "    <script>\n" +
                    "        function closeWindow() {\n" +
                    "            window.close();\n" +
                    "        }\n" +
                    "        // 3秒后自动关闭\n" +
                    "        setTimeout(closeWindow, 3000);\n" +
                    "    </script>\n" +
                    "</body>\n" +
                    "</html>";

            return html;
        } catch (Exception e) {
            logger.error("❌ 处理支付宝返回异常", e);
            return "<html><body><h2>支付返回处理异常</h2></body></html>";
        }
    }
    /**
     * 测试支付宝连接状态
     */
    @GetMapping("/test-connection")
    public Map<String, Object> testConnection() {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("🔄 测试支付宝连接...");

            // 尝试创建一个小的测试订单来验证连接
            String outTradeNo = "TEST" + System.currentTimeMillis();

            AlipayTradePagePayResponse response = Factory.Payment
                    .Page()
                    .pay("测试连接", outTradeNo, "0.01", "http://localhost:3000");

            if (response != null && response.getBody() != null) {
                logger.info("✅ 支付宝连接正常，返回表单长度: {}", response.getBody().length());

                result.put("success", true);
                result.put("message", "支付宝连接正常");
                result.put("testOrder", outTradeNo);
                result.put("formLength", response.getBody().length());
            } else {
                logger.error("❌ 支付宝返回空响应");
                result.put("success", false);
                result.put("message", "支付宝返回空响应");
            }

        } catch (Exception e) {
            logger.error("❌ 支付宝连接测试失败", e);
            result.put("success", false);
            result.put("message", "支付宝连接失败: " + e.getMessage());
            result.put("error", e.getClass().getName());
        }

        return result;
    }

    /**
     * 获取支付宝配置信息（用于调试）
     */
    @GetMapping("/config-info")
    public Map<String, Object> getConfigInfo() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 这里可以返回一些基本的配置信息（注意不要泄露敏感信息）
            result.put("success", true);
            result.put("gateway", "openapi-sandbox.dl.alipaydev.com");
            result.put("appId", "9021000157676949");
            result.put("notifyUrl", "http://x4622928.natappfree.cc/api/alipay/notify");
            result.put("returnUrl", "http://localhost:3000/external-user");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取配置信息失败: " + e.getMessage());
        }

        return result;
    }
}
