package com.example.campusparkingbackend.controller;

import com.example.campusparkingbackend.service.FinanceService;
import com.example.campusparkingbackend.service.UserService;
import com.example.campusparkingbackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/finance")
@CrossOrigin(origins = "*")
public class FinanceController {

    @Autowired
    private FinanceService financeService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取财务概览
     */
    @GetMapping("/overview")
    public ResponseEntity<?> getFinanceOverview(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }

            FinanceService.FinanceOverview overview = financeService.getFinanceOverview();
            return ResponseEntity.ok(new ApiResponse(true, "获取成功", overview));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取收入趋势
     */
    @GetMapping("/revenue-trend")
    public ResponseEntity<?> getRevenueTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }

            List<FinanceService.DailyRevenue> trend = financeService.getRevenueTrend(startDate, endDate);
            return ResponseEntity.ok(new ApiResponse(true, "获取成功", trend));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }
    @GetMapping("/test")
    public ResponseEntity<?> testApi(@RequestHeader("Authorization") String token) {
        try {
            System.out.println("✅ FinanceController测试端点被调用");
            System.out.println("🔑 Token: " + (token != null ? "存在" : "不存在"));

            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }

            // 返回简单的测试数据
            Map<String, Object> testData = new HashMap<>();
            testData.put("message", "API连接正常");
            testData.put("timestamp", LocalDateTime.now());
            testData.put("status", "OK");

            return ResponseEntity.ok(new ApiResponse(true, "测试成功", testData));
        } catch (Exception e) {
            System.err.println("❌ 测试端点异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ApiResponse(false, "测试失败: " + e.getMessage(), null));
        }
    }
    /**
     * 获取校外用户收费记录（分页）
     */
    @GetMapping("/external-records")
    public ResponseEntity<?> getExternalUserRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String plateNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }

            FinanceService.FinanceRecordPage records = financeService.getExternalUserRecords(page, size, plateNumber, startDate, endDate);
            return ResponseEntity.ok(new ApiResponse(true, "获取成功", records));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取月度统计
     */
    @GetMapping("/monthly-stats")
    public ResponseEntity<?> getMonthlyStats(
            @RequestParam int year,
            @RequestParam int month,
            @RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }

            FinanceService.MonthlyStats stats = financeService.getMonthlyStats(year, month);
            return ResponseEntity.ok(new ApiResponse(true, "获取成功", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取实时收入数据（最近30天）
     */
    @GetMapping("/recent-revenue")
    public ResponseEntity<?> getRecentRevenue(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);

            List<FinanceService.DailyRevenue> recentRevenue = financeService.getRevenueTrend(startDate, endDate);
            return ResponseEntity.ok(new ApiResponse(true, "获取成功", recentRevenue));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    // 权限验证方法
    private boolean isAdmin(String token) {
        try {
            String cleanToken = token.replace("Bearer ", "").trim();
            String role = jwtUtil.getRoleFromToken(cleanToken);
            return "ADMIN".equals(role);
        } catch (Exception e) {
            return false;
        }
    }

    // 统一响应类（复用您现有的）
    public static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;

        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }
}
