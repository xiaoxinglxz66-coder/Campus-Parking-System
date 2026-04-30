// src/main/java/com/example/campusparkingbackend/controller/StatisticsController.java
package com.example.campusparkingbackend.controller;

import com.example.campusparkingbackend.service.StatisticsService;
import com.example.campusparkingbackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取系统总体统计
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemStatistics(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).build();
            }

            StatisticsService.SystemStatistics stats = statisticsService.getSystemStatistics();

            // 转换为 Map 格式返回
            Map<String, Object> result = new HashMap<>();
            result.put("totalUsers", stats.getTotalUsers());
            result.put("pendingUsers", stats.getPendingUsers());
            result.put("approvedUsers", stats.getApprovedUsers());
            result.put("totalVehicles", stats.getTotalVehicles());
            result.put("pendingVehicles", stats.getPendingVehicles());
            result.put("approvedVehicles", stats.getApprovedVehicles());
            result.put("totalRecords", stats.getTotalRecords());
            result.put("parkingCount", stats.getParkingCount());
            result.put("completedCount", stats.getCompletedCount());
            result.put("totalRevenue", stats.getTotalRevenue());
            result.put("todayRevenue", stats.getTodayRevenue());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 获取今日统计
     */
    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayStatistics(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).build();
            }

            StatisticsService.TodayStatistics stats = statisticsService.getTodayStatistics();

            // 转换为 Map 格式返回
            Map<String, Object> result = new HashMap<>();
            result.put("todayNewUsers", stats.getTodayNewUsers());
            result.put("todayNewVehicles", stats.getTodayNewVehicles());
            result.put("todayParkingCount", stats.getTodayParkingCount());
            result.put("todayRevenue", stats.getTodayRevenue());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 获取收入统计（需要日期范围参数）
     */
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).build();
            }

            StatisticsService.RevenueStatistics stats = statisticsService.getRevenueStatistics(startDate, endDate);

            // 转换为 Map 格式返回
            Map<String, Object> result = new HashMap<>();
            result.put("totalRevenue", stats.getTotalRevenue());
            result.put("dailyRevenue", stats.getDailyRevenue());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 获取用户类型统计
     */
    @GetMapping("/user-types")
    public ResponseEntity<Map<String, Object>> getUserTypeStatistics(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).build();
            }

            StatisticsService.UserTypeStatistics stats = statisticsService.getUserTypeStatistics();

            // 转换为 Map 格式返回
            Map<String, Object> result = new HashMap<>();
            result.put("userCountByType", stats.getUserCountByType());
            result.put("vehicleCountByUserType", stats.getVehicleCountByUserType());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 获取车辆类型统计
     */
    @GetMapping("/vehicle-types")
    public ResponseEntity<Map<String, Object>> getVehicleTypeStatistics(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).build();
            }

            StatisticsService.VehicleTypeStatistics stats = statisticsService.getVehicleTypeStatistics();

            // 转换为 Map 格式返回
            Map<String, Object> result = new HashMap<>();
            result.put("vehicleCountByType", stats.getVehicleCountByType());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 获取系统概览（综合统计）
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getSystemOverview(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).build();
            }

            // 获取系统统计
            StatisticsService.SystemStatistics systemStats = statisticsService.getSystemStatistics();
            // 获取今日统计
            StatisticsService.TodayStatistics todayStats = statisticsService.getTodayStatistics();
            // 获取用户类型统计
            StatisticsService.UserTypeStatistics userTypeStats = statisticsService.getUserTypeStatistics();

            // 组合所有统计信息
            Map<String, Object> result = new HashMap<>();

            // 系统统计
            result.put("totalUsers", systemStats.getTotalUsers());
            result.put("pendingUsers", systemStats.getPendingUsers());
            result.put("totalVehicles", systemStats.getTotalVehicles());
            result.put("pendingVehicles", systemStats.getPendingVehicles());
            result.put("totalRecords", systemStats.getTotalRecords());
            result.put("currentParking", systemStats.getParkingCount());
            result.put("totalRevenue", systemStats.getTotalRevenue());

            // 今日统计
            result.put("todayNewUsers", todayStats.getTodayNewUsers());
            result.put("todayNewVehicles", todayStats.getTodayNewVehicles());
            result.put("todayParkingCount", todayStats.getTodayParkingCount());
            result.put("todayRevenue", todayStats.getTodayRevenue());

            // 用户类型分布
            result.put("userTypeDistribution", userTypeStats.getUserCountByType());
            result.put("vehicleUserTypeDistribution", userTypeStats.getVehicleCountByUserType());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 获取最近7天的收入统计
     */
    @GetMapping("/revenue/last7days")
    public ResponseEntity<Map<String, Object>> getLast7DaysRevenue(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).build();
            }

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(6); // 最近7天

            StatisticsService.RevenueStatistics stats = statisticsService.getRevenueStatistics(startDate, endDate);

            Map<String, Object> result = new HashMap<>();
            result.put("totalRevenue", stats.getTotalRevenue());
            result.put("dailyRevenue", stats.getDailyRevenue());
            result.put("startDate", startDate);
            result.put("endDate", endDate);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 获取最近30天的收入统计
     */
    @GetMapping("/revenue/last30days")
    public ResponseEntity<Map<String, Object>> getLast30DaysRevenue(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).build();
            }

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(29); // 最近30天

            StatisticsService.RevenueStatistics stats = statisticsService.getRevenueStatistics(startDate, endDate);

            Map<String, Object> result = new HashMap<>();
            result.put("totalRevenue", stats.getTotalRevenue());
            result.put("dailyRevenue", stats.getDailyRevenue());
            result.put("startDate", startDate);
            result.put("endDate", endDate);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 工具方法
    private boolean isAdmin(String token) {
        try {
            String role = jwtUtil.getRoleFromToken(cleanToken(token));
            return "ADMIN".equals(role);
        } catch (Exception e) {
            return false;
        }
    }

    private String cleanToken(String token) {
        return token.replace("Bearer ", "").trim();
    }
}
