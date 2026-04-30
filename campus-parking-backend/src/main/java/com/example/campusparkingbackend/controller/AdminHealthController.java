// src/main/java/com/example/campusparkingbackend/controller/AdminHealthController.java
package com.example.campusparkingbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/health")
@CrossOrigin(origins = "*")
public class AdminHealthController {

    @Autowired
    private DataSource dataSource;

    /**
     * 检查数据库连接状态
     */
    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> checkDatabaseHealth() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 测试数据库连接
            try (Connection conn = dataSource.getConnection()) {
                if (conn.isValid(2)) { // 2秒超时
                    response.put("status", "healthy");
                    response.put("message", "数据库连接正常");
                    response.put("success", true);
                } else {
                    response.put("status", "unhealthy");
                    response.put("message", "数据库连接异常");
                    response.put("success", false);
                }
            }

            // 添加更多数据库统计信息
            response.put("timestamp", System.currentTimeMillis());
            response.put("service", "database");

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "数据库连接失败: " + e.getMessage());
            response.put("success", false);
            response.put("error", e.getClass().getName());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 检查API服务状态
     */
    @GetMapping("/api")
    public ResponseEntity<Map<String, Object>> checkApiHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("message", "API服务运行正常");
        response.put("success", true);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * 综合系统健康检查
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> checkSystemHealth() {
        Map<String, Object> response = new HashMap<>();

        // 这里可以添加更多的健康检查项
        response.put("database", checkDatabaseHealth().getBody());
        response.put("api", "healthy");
        response.put("memory", getMemoryUsage());
        response.put("uptime", getSystemUptime());
        response.put("success", true);

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> getMemoryUsage() {
        Map<String, Object> memory = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        memory.put("total", runtime.totalMemory() / (1024 * 1024) + " MB");
        memory.put("free", runtime.freeMemory() / (1024 * 1024) + " MB");
        memory.put("max", runtime.maxMemory() / (1024 * 1024) + " MB");
        memory.put("used", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) + " MB");
        return memory;
    }

    private Map<String, Object> getSystemUptime() {
        Map<String, Object> uptime = new HashMap<>();
        long uptimeMillis = System.currentTimeMillis() - java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime();
        uptime.put("milliseconds", uptimeMillis);
        uptime.put("formatted", formatUptime(uptimeMillis));
        return uptime;
    }

    private String formatUptime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        return String.format("%d天 %02d:%02d:%02d",
                days, hours % 24, minutes % 60, seconds % 60);
    }
}
