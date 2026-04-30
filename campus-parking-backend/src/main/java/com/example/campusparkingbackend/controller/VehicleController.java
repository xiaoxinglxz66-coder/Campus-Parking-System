// src/main/java/com/example/campusparkingbackend/controller/VehicleController.java
package com.example.campusparkingbackend.controller;

import com.example.campusparkingbackend.entity.Vehicle;
import com.example.campusparkingbackend.entity.User;
import com.example.campusparkingbackend.repository.ParkingRecordRepository;
import com.example.campusparkingbackend.service.VehicleService;
import com.example.campusparkingbackend.util.JwtUtil;
import com.example.campusparkingbackend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import com.example.campusparkingbackend.repository.VehicleRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.example.campusparkingbackend.dto.VehicleDTO;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*")
public class VehicleController {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    /**
     * 获取所有车辆（管理员权限）
     */
    @GetMapping
    public ResponseEntity<List<Vehicle>> getAllVehicles(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            System.out.println("=== 获取所有车辆请求 ===");

            // 严格的权限检查
            if (token == null || token.trim().isEmpty()) {
                System.out.println("❌ Token为空");
                return ResponseEntity.status(401).body(Collections.emptyList());
            }

            String cleanToken = token.replace("Bearer ", "").trim();

            // 验证Token
            if (!jwtUtil.validateToken(cleanToken)) {
                System.out.println("❌ Token验证失败");
                return ResponseEntity.status(401).body(Collections.emptyList());
            }

            // 检查管理员权限
            String role = jwtUtil.getRoleFromToken(cleanToken);
            String username = jwtUtil.getUsernameFromToken(cleanToken);
            System.out.println("用户: " + username + ", 角色: " + role);

            if (!"ADMIN".equals(role)) {
                System.out.println("❌ 权限不足，需要管理员权限，当前角色: " + role);
                return ResponseEntity.status(403).body(Collections.emptyList());
            }

            System.out.println("✅ 管理员权限验证通过，查询所有车辆");
            List<Vehicle> vehicles = vehicleService.getAllVehicles();
            System.out.println("✅ 返回所有车辆数量: " + vehicles.size());

            return ResponseEntity.ok(vehicles);

        } catch (Exception e) {
            System.out.println("❌ 获取所有车辆异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }
    /**
     * 数据库检查接口
     */
    @GetMapping("/debug/db-check")
    public ResponseEntity<Map<String, Object>> debugDatabaseCheck(@RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 数据库检查接口 ===");

            Map<String, Object> result = new HashMap<>();

            // 1. 检查车辆表
            long vehicleCount = vehicleRepository.count();
            List<Vehicle> allVehicles = vehicleRepository.findAll();

            result.put("vehicleCount", vehicleCount);
            result.put("vehicles", allVehicles.stream().map(v -> {
                Map<String, Object> vehicleInfo = new HashMap<>();
                vehicleInfo.put("id", v.getId());
                vehicleInfo.put("plateNumber", v.getPlateNumber());
                vehicleInfo.put("status", v.getStatus());
                vehicleInfo.put("isTemporary", v.getIsTemporary());
                vehicleInfo.put("user", v.getUser() != null ?
                        v.getUser().getUsername() + " (" + v.getUser().getRealName() + ")" : "null");
                return vehicleInfo;
            }).collect(Collectors.toList()));

            // 2. 检查用户表
            List<User> allUsers = userRepository.findAll();
            result.put("userCount", allUsers.size());
            result.put("users", allUsers.stream().map(u ->
                    u.getUsername() + " (" + u.getRealName() + " - " + u.getUserType() + ")"
            ).collect(Collectors.toList()));

            System.out.println("✅ 数据库检查完成:");
            System.out.println("  车辆总数: " + vehicleCount);
            System.out.println("  用户总数: " + allUsers.size());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.out.println("❌ 数据库检查异常: " + e.getMessage());
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    @GetMapping("/all-dto")
    public ResponseEntity<List<VehicleDTO>> getAllUserVehiclesDTO(@RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== /api/vehicles/all-dto 接口调用 ===");

            // 验证权限
            String cleanToken = token.replace("Bearer ", "").trim();
            if (!jwtUtil.validateToken(cleanToken)) {
                return ResponseEntity.status(401).body(Collections.emptyList());
            }

            String role = jwtUtil.getRoleFromToken(cleanToken);
            System.out.println("调用者角色: " + role);

            // 这个接口现在只给管理员用，并且只返回校内用户数据
            if (!"ADMIN".equals(role)) {
                System.out.println("❌ 非管理员禁止访问 all-dto 接口");
                return ResponseEntity.status(403).body(Collections.emptyList());
            }

            // 调用新的方法，只返回校内用户车辆
            List<VehicleDTO> dtos = vehicleService.getAllCampusUserVehiclesDTO();
            System.out.println("✅ all-dto 返回数据数量: " + dtos.size());

            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            System.err.println("❌ all-dto 接口异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }

    /**
     * 测试接口：直接返回测试数据
     */
    @GetMapping("/test-data")
    public ResponseEntity<List<Map<String, Object>>> getTestData(@RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 测试接口：返回测试数据 ===");

            List<Map<String, Object>> testData = new ArrayList<>();

            // 添加测试数据
            Map<String, Object> vehicle1 = new HashMap<>();
            vehicle1.put("id", 1L);
            vehicle1.put("plateNumber", "测试车牌A12345");
            vehicle1.put("vehicleType", "CAR");
            vehicle1.put("brand", "测试品牌");
            vehicle1.put("color", "黑色");
            vehicle1.put("status", "APPROVED");
            vehicle1.put("isTemporary", false);
            vehicle1.put("createdAt", LocalDateTime.now().toString());
            vehicle1.put("updatedAt", LocalDateTime.now().toString());

            Map<String, Object> user1 = new HashMap<>();
            user1.put("id", 1L);
            user1.put("username", "testuser");
            user1.put("realName", "测试用户");
            user1.put("userType", "STUDENT");
            vehicle1.put("user", user1);

            testData.add(vehicle1);

            System.out.println("✅ 返回测试数据: " + testData.size() + " 条");
            return ResponseEntity.ok(testData);

        } catch (Exception e) {
            System.out.println("❌ 测试接口异常: " + e.getMessage());
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }
    /**
     * 根据ID获取车辆
     */
    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        Optional<Vehicle> vehicle = vehicleService.getVehicleById(id);
        return vehicle.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    /**
     * 获取所有用户的车辆（管理员权限）
     */
    @GetMapping("/all")
    public ResponseEntity<List<Vehicle>> getAllUserVehicles(@RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 获取所有用户车辆 ===");

            // 检查管理员权限
            String cleanToken = token.replace("Bearer ", "").trim();
            if (!jwtUtil.validateToken(cleanToken)) {
                return ResponseEntity.status(401).body(Collections.emptyList());
            }

            String role = jwtUtil.getRoleFromToken(cleanToken);
            if (!"ADMIN".equals(role)) {
                System.out.println("❌ 权限不足，需要管理员权限，当前角色: " + role);
                return ResponseEntity.status(403).body(Collections.emptyList());
            }

            System.out.println("✅ 管理员权限验证通过，查询所有用户车辆");

            // 获取所有车辆（包括所有用户）
            List<Vehicle> allVehicles = vehicleService.getAllVehicles();
            System.out.println("✅ 数据库查询结果数量: " + allVehicles.size());

            // 调试：打印前几个车辆信息
            for (int i = 0; i < Math.min(allVehicles.size(), 3); i++) {
                Vehicle v = allVehicles.get(i);
                System.out.println("车辆 " + (i+1) + ": ID=" + v.getId() +
                        ", 车牌=" + v.getPlateNumber() +
                        ", 状态=" + v.getStatus() +
                        ", 用户ID=" + (v.getUser() != null ? v.getUser().getId() : "null"));
            }

            return ResponseEntity.ok(allVehicles);

        } catch (Exception e) {
            System.out.println("❌ 获取所有用户车辆异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }
    /**
     * 创建车辆
     */
    @PostMapping
    public ResponseEntity<?> createVehicle(@RequestBody Vehicle vehicle) {
        try {
            Vehicle savedVehicle = vehicleService.createVehicle(vehicle);
            return ResponseEntity.ok(savedVehicle);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    /**
     * 强制删除车辆（管理员专用）
     */
    @PostMapping("/{id}/force-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> forceDeleteVehicle(@PathVariable Long id) {
        try {
            System.out.println("🔧 强制删除车辆 ID: " + id);

            // 1. 检查车辆是否存在
            Vehicle vehicle = vehicleService.getVehicleById(id)
                    .orElseThrow(() -> new RuntimeException("车辆不存在，ID: " + id));

            System.out.println("🔍 车辆信息: " + vehicle.getPlateNumber());

            // 2. 先尝试删除所有可能的 parking_records 表
            String[] tables = {"parking_records", "parking_records_copy", "parking_records_copy2"};
            int totalDeleted = 0;

            for (String table : tables) {
                try {
                    int deleted = entityManager.createNativeQuery(
                                    "DELETE FROM " + table + " WHERE vehicle_id = :vehicleId")
                            .setParameter("vehicleId", id)
                            .executeUpdate();

                    if (deleted > 0) {
                        System.out.println("🗑️ 删除表 " + table + " 记录: " + deleted + " 条");
                        totalDeleted += deleted;
                    }
                } catch (Exception e) {
                    System.out.println("ℹ️ 表 " + table + " 不存在或已处理: " + e.getMessage());
                }
            }

            // 3. 删除车辆
            vehicleService.deleteVehicle(id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "车辆强制删除成功",
                    "deletedRecords", totalDeleted,
                    "vehiclePlate", vehicle.getPlateNumber()
            ));

        } catch (Exception e) {
            System.err.println("❌ 强制删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "errorType", e.getClass().getSimpleName()
            ));
        }
    }
    /**
     * 检查车辆约束
     */
    @GetMapping("/{id}/check-constraints")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> checkVehicleConstraints(@PathVariable Long id) {
        try {
            System.out.println("🔍 检查车辆约束 ID: " + id);

            Map<String, Object> result = Map.of(
                    "vehicleId", id,
                    "existsInVehicles", vehicleService.getVehicleById(id).isPresent(),
                    "parkingRecordsCount", entityManager.createNativeQuery(
                                    "SELECT COUNT(*) FROM parking_records WHERE vehicle_id = :vehicleId", Long.class)
                            .setParameter("vehicleId", id)
                            .getSingleResult(),
                    "parkingRecordsCopy2Count", getTableRecordCount("parking_records_copy2", id),
                    "foreignKeyConstraints", checkForeignKeyConstraints(id)
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    private Long getTableRecordCount(String tableName, Long vehicleId) {
        try {
            return (Long) entityManager.createNativeQuery(
                            "SELECT COUNT(*) FROM " + tableName + " WHERE vehicle_id = :vehicleId", Long.class)
                    .setParameter("vehicleId", vehicleId)
                    .getSingleResult();
        } catch (Exception e) {
            return -1L; // 表不存在
        }
    }

    private Map<String, Object> checkForeignKeyConstraints(Long vehicleId) {
        try {
            // 查询所有引用 vehicles 表的约束
            return (Map<String, Object>) entityManager.createNativeQuery(
                            "SELECT " +
                                    "    TABLE_NAME, " +
                                    "    CONSTRAINT_NAME, " +
                                    "    REFERENCED_TABLE_NAME, " +
                                    "    REFERENCED_COLUMN_NAME " +
                                    "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                                    "WHERE REFERENCED_TABLE_NAME = 'vehicles' " +
                                    "    AND REFERENCED_COLUMN_NAME = 'id'", Map.class)
                    .getSingleResult();
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
    /**
     * 创建用户自己的车辆（安全接口）
     */
    @PostMapping("/my-vehicles")
    public ResponseEntity<?> createMyVehicle(@RequestBody Vehicle vehicle,
                                             @RequestHeader("Authorization") String token) {
        try {
            String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 设置当前用户
            vehicle.setUser(user);
            Vehicle savedVehicle = vehicleService.createVehicle(vehicle);
            return ResponseEntity.ok(savedVehicle);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 更新车辆信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicle(@PathVariable Long id, @RequestBody Vehicle vehicleDetails) {
        try {
            Vehicle updatedVehicle = vehicleService.updateVehicle(id, vehicleDetails);
            return ResponseEntity.ok(updatedVehicle);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 删除车辆
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id) {
        try {
            vehicleService.deleteVehicle(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveVehicle(@PathVariable Long id,
                                            @RequestBody ApproveRequest request,
                                            @RequestHeader("Authorization") String token) {
        try {
            // 检查管理员权限
            String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("权限不足，需要管理员权限");
            }

            Vehicle vehicle = vehicleService.approveVehicle(id, request.getApproved(), request.getReviewComment());
            return ResponseEntity.ok(vehicle);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 批量审核车辆（管理员权限）
     */
    @PostMapping("/batch-approve")
    public ResponseEntity<?> batchApproveVehicles(@RequestBody BatchApproveRequest request,
                                                  @RequestHeader("Authorization") String token) {
        try {
            // 检查管理员权限
            String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("权限不足，需要管理员权限");
            }

            List<Vehicle> vehicles = vehicleService.batchApproveVehicles(request.getVehicleIds(), request.getApproved());
            return ResponseEntity.ok(vehicles);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取待审核车辆（管理员权限）
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Vehicle>> getPendingVehicles(@RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== VehicleController: 处理待审核车辆请求 ===");
            System.out.println("原始Token: " + token);

            if (token == null || token.isEmpty()) {
                System.out.println("❌ Token为空");
                return ResponseEntity.status(401).body(Collections.emptyList());
            }

            // 清理 Token
            String cleanToken = token.replace("Bearer ", "").trim();
            System.out.println("清理后Token: " + cleanToken);

            // 验证 Token
            if (!jwtUtil.validateToken(cleanToken)) {
                System.out.println("❌ Token验证失败");
                return ResponseEntity.status(401).body(Collections.emptyList());
            }

            // 获取角色
            String role = jwtUtil.getRoleFromToken(cleanToken);
            String username = jwtUtil.getUsernameFromToken(cleanToken);
            System.out.println("用户: " + username + ", 角色: " + role);

            if (!"ADMIN".equals(role)) {
                System.out.println("❌ 权限不足，需要管理员，当前角色: " + role);
                return ResponseEntity.status(403).body(Collections.emptyList());
            }

            System.out.println("✅ 权限验证通过，开始查询数据");
            List<Vehicle> vehicles = vehicleService.getPendingVehicles();
            System.out.println("返回数据数量: " + vehicles.size());

            return ResponseEntity.ok(vehicles);

        } catch (Exception e) {
            System.out.println("❌ 处理请求异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(403).body(Collections.emptyList());
        }
    }

    /**
     * 获取已审核车辆（管理员权限）
     */
    @GetMapping("/approved")
    public ResponseEntity<List<Vehicle>> getApprovedVehicles(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 如果有token，检查管理员权限
            if (token != null && !token.isEmpty()) {
                String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));
                if (!"ADMIN".equals(role)) {
                    return ResponseEntity.status(403).body(null);
                }
            }

            List<Vehicle> vehicles = vehicleService.getApprovedVehicles();
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            System.out.println("获取已审核车辆异常: " + e.getMessage());
            return ResponseEntity.status(403).body(null);
        }
    }

    /**
     * 获取用户车辆
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Vehicle>> getVehiclesByUserId(@PathVariable Long userId) {
        List<Vehicle> vehicles = vehicleService.getVehiclesByUserId(userId);
        return ResponseEntity.ok(vehicles);
    }

    /**
     * 获取用户自己的车辆 - 使用投影避免循环引用
     */
    @GetMapping("/my-vehicles")
    public ResponseEntity<List<Map<String, Object>>> getMyVehiclesProjection(@RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 使用投影获取车辆 ===");

            // 1. Token验证
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.status(401).body(Collections.emptyList());
            }

            String cleanToken = token.replace("Bearer ", "").trim();
            if (!jwtUtil.validateToken(cleanToken)) {
                return ResponseEntity.status(401).body(Collections.emptyList());
            }

            // 2. 获取用户信息
            String username = jwtUtil.getUsernameFromToken(cleanToken);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

            System.out.println("找到用户: ID=" + user.getId() + ", 用户名=" + user.getUsername());

            // 3. 查询车辆
            List<Vehicle> vehicles = vehicleService.getVehiclesByUserId(user.getId());
            System.out.println("✅ 成功获取车辆数量: " + vehicles.size());

            // 4. 创建投影数据
            List<Map<String, Object>> projection = new ArrayList<>();
            for (Vehicle vehicle : vehicles) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", vehicle.getId());
                map.put("plateNumber", vehicle.getPlateNumber());
                map.put("vehicleType", vehicle.getVehicleType());
                map.put("brand", vehicle.getBrand());
                map.put("color", vehicle.getColor());
                map.put("status", vehicle.getStatus());
                map.put("isTemporary", vehicle.getIsTemporary());
                map.put("createdAt", vehicle.getCreatedAt());

                // 如果需要用户信息，只包含必要字段
                if (vehicle.getUser() != null) {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", vehicle.getUser().getId());
                    userInfo.put("username", vehicle.getUser().getUsername());
                    userInfo.put("realName", vehicle.getUser().getRealName());
                    map.put("user", userInfo);
                }

                projection.add(map);
            }

            System.out.println("✅ 投影数据创建完成，车辆数量: " + projection.size());
            return ResponseEntity.ok(projection);

        } catch (Exception e) {
            System.err.println("❌ 获取车辆投影失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }
    // 在 VehicleController.java 中添加测试接口
    @GetMapping("/test-token")
    public ResponseEntity<?> testToken(@RequestHeader("Authorization") String token) {
        System.out.println("=== 测试Token接口 ===");

        try {
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Token为空");
            }

            String cleanToken = token.replace("Bearer ", "").trim();
            System.out.println("清理后Token: " + cleanToken);

            // 验证Token
            boolean isValid = jwtUtil.validateToken(cleanToken);
            System.out.println("Token验证结果: " + isValid);

            if (!isValid) {
                return ResponseEntity.badRequest().body("Token无效");
            }

            // 获取用户名
            String username = jwtUtil.getUsernameFromToken(cleanToken);
            System.out.println("用户名: " + username);

            // 获取角色
            String role = jwtUtil.getRoleFromToken(cleanToken);
            System.out.println("角色: " + role);

            Map<String, Object> result = new HashMap<>();
            result.put("username", username);
            result.put("role", role);
            result.put("valid", true);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("测试Token失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("测试失败: " + e.getMessage());
        }
    }
    // 临时在 VehicleController 中添加手动测试方法
    @GetMapping("/test-db")
    public ResponseEntity<?> testDatabase() {
        try {
            System.out.println("=== 测试数据库连接 ===");

            // 测试用户查询
            List<User> allUsers = userRepository.findAll();
            System.out.println("用户总数: " + allUsers.size());

            // 测试车辆查询
            List<Vehicle> allVehicles = vehicleRepository.findAll();
            System.out.println("车辆总数: " + allVehicles.size());

            // 测试特定用户查询
            Optional<User> adminUser = userRepository.findByUsername("admin");
            if (adminUser.isPresent()) {
                User user = adminUser.get();
                System.out.println("管理员用户: ID=" + user.getId() + ", 用户名=" + user.getUsername());

                List<Vehicle> userVehicles = vehicleRepository.findByUserId(user.getId());
                System.out.println("管理员车辆数量: " + userVehicles.size());
            }

            return ResponseEntity.ok("数据库测试完成");

        } catch (Exception e) {
            System.err.println("数据库测试失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("数据库测试失败: " + e.getMessage());
        }
    }

    /**
     * 获取校内用户车辆（管理员权限）
     */
    @GetMapping("/campus")
    public ResponseEntity<List<Vehicle>> getCampusUserVehicles(@RequestHeader("Authorization") String token) {
        try {
            // 检查管理员权限
            String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body(null);
            }

            List<Vehicle> vehicles = vehicleService.getCampusUserVehicles();
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null);
        }
    }

    /**
     * 获取临时车辆（管理员权限）
     */
    @GetMapping("/temporary")
    public ResponseEntity<List<Vehicle>> getTemporaryVehicles(@RequestHeader("Authorization") String token) {
        try {
            // 检查管理员权限
            String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body(null);
            }

            List<Vehicle> vehicles = vehicleService.getTemporaryVehicles();
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null);
        }
    }

    /**
     * 根据车牌号查找车辆
     */
    @GetMapping("/search")
    public ResponseEntity<Vehicle> getVehicleByPlateNumber(@RequestParam String plateNumber) {
        Optional<Vehicle> vehicle = vehicleService.getVehicleByPlateNumber(plateNumber);
        return vehicle.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    /**
     * 调试接口：获取所有待审核车辆（不区分用户类型）
     */
    @GetMapping("/debug/pending-all")
    public ResponseEntity<List<Vehicle>> getAllPendingVehicles(@RequestHeader("Authorization") String token) {
        try {
            System.out.println("=== 调试接口：获取所有PENDING车辆 ===");

            // 检查管理员权限
            String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body(Collections.emptyList());
            }

            // 直接查询所有PENDING状态的车辆
            List<Vehicle> allPending = vehicleRepository.findByStatus(Vehicle.VehicleStatus.PENDING);
            System.out.println("调试接口返回: " + allPending.size() + " 辆PENDING车辆");

            return ResponseEntity.ok(allPending);
        } catch (Exception e) {
            System.err.println("调试接口异常: " + e.getMessage());
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }

    /**
     * 获取车辆统计信息（管理员权限）
     */
    @GetMapping("/stats")
    public ResponseEntity<VehicleService.VehicleStats> getVehicleStatistics(@RequestHeader("Authorization") String token) {
        try {
            // 检查管理员权限
            String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body(null);
            }

            VehicleService.VehicleStats stats = vehicleService.getVehicleStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null);
        }
    }

    // 请求体类
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
        private List<Long> vehicleIds;
        private Boolean approved;

        // Getters and Setters
        public List<Long> getVehicleIds() { return vehicleIds; }
        public void setVehicleIds(List<Long> vehicleIds) { this.vehicleIds = vehicleIds; }
        public Boolean getApproved() { return approved; }
        public void setApproved(Boolean approved) { this.approved = approved; }
    }
}
