// src/main/java/com/example/campusparkingbackend/service/VehicleService.java
package com.example.campusparkingbackend.service;

import com.example.campusparkingbackend.dto.VehicleDTO;
import com.example.campusparkingbackend.entity.ParkingRecord;
import com.example.campusparkingbackend.entity.User;
import com.example.campusparkingbackend.entity.Vehicle;
import com.example.campusparkingbackend.repository.UserRepository;
import com.example.campusparkingbackend.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.campusparkingbackend.repository.ParkingRecordRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    // 在 VehicleService 类中添加
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ParkingRecordService parkingRecordService;
    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ParkingRecordRepository parkingRecordRepository;

    /**
     * 根据ID获取车辆
     */
    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleRepository.findById(id);
    }

    /**
     * 创建车辆（校内用户需要审核，校外用户临时车辆自动审核）
     */
    @Transactional
    public Vehicle createVehicle(Vehicle vehicle) {
        System.out.println("=== 开始创建车辆 ===");
        System.out.println("车牌号: " + vehicle.getPlateNumber());
        System.out.println("用户ID: " + (vehicle.getUser() != null ? vehicle.getUser().getId() : "null"));

        // 检查车牌号是否已存在（非临时车辆）
        if (!Boolean.TRUE.equals(vehicle.getIsTemporary()) &&
                vehicleRepository.existsByPlateNumber(vehicle.getPlateNumber())) {
            throw new RuntimeException("车牌号已存在: " + vehicle.getPlateNumber());
        }

        // 检查用户是否存在
        if (vehicle.getUser() == null || vehicle.getUser().getId() == null) {
            throw new RuntimeException("用户信息不能为空");
        }

        User user = userRepository.findById(vehicle.getUser().getId())
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + vehicle.getUser().getId()));

        System.out.println("找到用户: " + user.getUsername() + ", 类型: " + user.getUserType());

        vehicle.setUser(user);

        // 设置审核状态 - 确保校内用户必须审核
        if (Boolean.TRUE.equals(vehicle.getIsTemporary())) {
            // 临时车辆自动审核通过
            vehicle.setStatus(Vehicle.VehicleStatus.APPROVED);
            System.out.println("临时车辆，自动审核通过");
        } else {
            // 校内用户车辆需要审核
            if (user.getUserType() != User.UserType.EXTERNAL_USER) {
                vehicle.setStatus(Vehicle.VehicleStatus.PENDING);
                System.out.println("校内用户车辆，状态设置为: PENDING");
            } else {
                // 校外用户永久车辆也自动审核
                vehicle.setStatus(Vehicle.VehicleStatus.APPROVED);
                System.out.println("校外用户车辆，自动审核通过");
            }
        }

        vehicle.setCreatedAt(LocalDateTime.now());
        vehicle.setUpdatedAt(LocalDateTime.now());

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        System.out.println("车辆创建成功，ID: " + savedVehicle.getId() + ", 状态: " + savedVehicle.getStatus());

        return savedVehicle;
    }

    /**
     * 更新车辆信息 - 修复版本
     */
    @Transactional
    public Vehicle updateVehicle(Long id, Vehicle vehicleDetails) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("车辆不存在，ID: " + id));

        System.out.println("=== 更新车辆信息 ===");
        System.out.println("目标车辆ID: " + id);
        System.out.println("车牌号: " + vehicle.getPlateNumber());

        // 🆕 重要修复：检查并打印接收到的数据
        System.out.println("接收到的更新数据:");
        System.out.println("  plateNumber: " + vehicleDetails.getPlateNumber());
        System.out.println("  vehicleType: " + vehicleDetails.getVehicleType());
        System.out.println("  brand: " + vehicleDetails.getBrand());
        System.out.println("  color: " + vehicleDetails.getColor());
        System.out.println("  status: " + vehicleDetails.getStatus());
        System.out.println("  user: " + (vehicleDetails.getUser() != null ? vehicleDetails.getUser().getId() : "null"));

        // 🆕 关键修复：只有在提供新车牌号时才更新（并且不能为null）
        if (vehicleDetails.getPlateNumber() != null &&
                !vehicleDetails.getPlateNumber().trim().isEmpty() &&
                !vehicle.getPlateNumber().equals(vehicleDetails.getPlateNumber())) {

            // 检查新车牌号是否已存在（非临时车辆）
            if (!Boolean.TRUE.equals(vehicle.getIsTemporary()) &&
                    vehicleRepository.existsByPlateNumber(vehicleDetails.getPlateNumber())) {
                throw new RuntimeException("车牌号已存在: " + vehicleDetails.getPlateNumber());
            }

            vehicle.setPlateNumber(vehicleDetails.getPlateNumber().trim());
            System.out.println("✅ 车牌号更新: " + vehicle.getPlateNumber());
        } else {
            // 🆕 保持原有车牌号不变
            System.out.println("⚠️ 车牌号未提供或为空，保持原值: " + vehicle.getPlateNumber());
        }

        // 更新其他可修改的字段
        if (vehicleDetails.getVehicleType() != null) {
            vehicle.setVehicleType(vehicleDetails.getVehicleType());
        }

        if (vehicleDetails.getBrand() != null) {
            vehicle.setBrand(vehicleDetails.getBrand().trim());
        }

        if (vehicleDetails.getColor() != null) {
            vehicle.setColor(vehicleDetails.getColor().trim());
        }

        // 🆕 重要：不要更新以下字段（它们应该在创建时设置）
        // - status: 审核状态应该通过专门的审核接口修改
        // - isTemporary: 是否临时车辆应该在创建时确定
        // - user: 车辆所属用户不应该被修改
        // - createdAt: 创建时间应该保持不变

        vehicle.setUpdatedAt(LocalDateTime.now());

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        System.out.println("✅ 车辆更新成功，ID: " + savedVehicle.getId());

        return savedVehicle;
    }
    /**
     * 根据用户ID和用户类型获取车辆（数据隔离核心方法）
     */
    public List<Vehicle> getVehiclesByUserIdAndUserType(Long userId, User.UserType userType) {
        // 验证用户类型匹配
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));

        if (user.getUserType() != userType) {
            throw new RuntimeException("用户类型不匹配");
        }

        return vehicleRepository.findByUserIdAndUserUserType(userId, userType);
    }

    /**
     * 获取用户自己的车辆（安全方法）
     */
    public List<Vehicle> getUserOwnVehicles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));

        return vehicleRepository.findByUserIdAndUserUserType(userId, user.getUserType());
    }

    /**
     * 创建车辆时强制用户类型匹配
     */
    @Transactional
    public Vehicle createVehicleWithUserCheck(Vehicle vehicle, Long userId) {
        // 检查用户是否存在且类型匹配
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));

        if (!user.getId().equals(vehicle.getUser().getId())) {
            throw new RuntimeException("用户ID不匹配");
        }

        vehicle.setUser(user);
        return createVehicle(vehicle);
    }
    /**
     * 获取所有校内用户的车辆（排除校外用户） - 完全同步数据库
     */
    public List<Vehicle> getAllCampusUserVehicles() {
        try {
            System.out.println("=== 查询所有校内用户车辆（同步数据库） ===");

            List<User.UserType> campusUserTypes = List.of(
                    User.UserType.STUDENT,
                    User.UserType.TEACHER,
                    User.UserType.STAFF
            );

            // 方法1: 使用 JPQL 查询（推荐）
            System.out.println("🔍 使用 JPQL 查询校内用户车辆...");
            List<Vehicle> campusVehicles = vehicleRepository.findByUserUserTypeInAndIsTemporaryFalse(campusUserTypes);

            System.out.println("✅ 数据库查询完成，找到 " + campusVehicles.size() + " 辆校内用户车辆");

            // 详细日志
            for (Vehicle vehicle : campusVehicles) {
                System.out.println("  车牌: " + vehicle.getPlateNumber() +
                        ", 状态: " + vehicle.getStatus() +
                        ", 用户: " + (vehicle.getUser() != null ?
                        vehicle.getUser().getRealName() + " (" + vehicle.getUser().getUserType() + ")" : "未知"));
            }

            return campusVehicles;

        } catch (Exception e) {
            System.err.println("❌ 查询校内用户车辆失败: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    @Transactional
    public void deleteVehicle(Long id) {
        try {
            System.out.println("=== 开始删除车辆，ID: " + id + " ===");

            Vehicle vehicle = vehicleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("车辆不存在，ID: " + id));

            System.out.println("找到车辆: " + vehicle.getPlateNumber());

            // 检查车辆是否正在停车中
            Optional<Vehicle> currentParkingVehicle = vehicleRepository.findVehicleInParking(id);
            if (currentParkingVehicle.isPresent()) {
                System.out.println("❌ 车辆正在停车中，无法删除");
                throw new RuntimeException("车辆正在停车中，无法删除");
            }

            // 删除该车辆的所有停车记录
            List<ParkingRecord> vehicleRecords = parkingRecordRepository.findByVehicleId(id);
            System.out.println("找到相关停车记录: " + vehicleRecords.size() + " 条");

            if (!vehicleRecords.isEmpty()) {
                try {
                    parkingRecordRepository.deleteAll(vehicleRecords);
                    System.out.println("✅ 成功删除停车记录");
                } catch (Exception e) {
                    System.err.println("❌ 删除停车记录失败: " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException("删除停车记录失败: " + e.getMessage());
                }
            }

            // 删除车辆
            try {
                vehicleRepository.deleteById(id);
                System.out.println("✅ 车辆删除成功");
            } catch (Exception e) {
                System.err.println("❌ 删除车辆失败: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("删除车辆失败: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("❌ deleteVehicle 方法异常: " + e.getMessage());
            e.printStackTrace();
            throw e; // 重要：重新抛出异常，不要吞掉
        }
    }
    /**
     * 删除车辆的所有关联记录
     */
    private void deleteAllRelatedRecords(Long vehicleId) {
        System.out.println("🔍 开始删除车辆的所有关联记录...");

        try {
            // 1. 删除 parking_records 表中的记录
            int deletedRecords = parkingRecordRepository.deleteByVehicleId(vehicleId);
            System.out.println("✅ 删除 parking_records: " + deletedRecords + " 条");

            // 2. 尝试删除 parking_records_copy2 表中的记录（如果存在）
            try {
                // 如果有 parking_records_copy2 表
                entityManager.createNativeQuery(
                                "DELETE FROM parking_records_copy2 WHERE vehicle_id = :vehicleId")
                        .setParameter("vehicleId", vehicleId)
                        .executeUpdate();
                System.out.println("✅ 删除 parking_records_copy2 记录");
            } catch (Exception e) {
                System.out.println("ℹ️ parking_records_copy2 表不存在或已处理: " + e.getMessage());
            }

            // 3. 尝试删除其他可能的关联表记录
            try {
                entityManager.createNativeQuery(
                                "DELETE FROM parking_records WHERE vehicle_id = :vehicleId")
                        .setParameter("vehicleId", vehicleId)
                        .executeUpdate();
            } catch (Exception e) {
                System.out.println("⚠️ 直接SQL删除也失败: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("❌ 删除关联记录失败: " + e.getMessage());
            throw new RuntimeException("无法删除关联的停车记录，请先手动清理", e);
        }
    }

    // 🆕 添加一个辅助方法
    private List<ParkingRecord> findParkingRecordsByVehicleId(Long vehicleId) {
        try {
            // 尝试使用 repository 的方法
            return parkingRecordRepository.findByVehicleId(vehicleId);
        } catch (Exception e) {
            // 如果方法不存在，使用查询
            System.out.println("findByVehicleId 方法不存在，使用JPQL查询");
            return parkingRecordRepository.findAll().stream()
                    .filter(record -> record.getVehicle() != null &&
                            record.getVehicle().getId().equals(vehicleId))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 根据用户ID查找车辆
     */
    public List<Vehicle> getVehiclesByUserId(Long userId) {
        return vehicleRepository.findByUserId(userId);
    }

    /**
     * 根据车辆类型查找
     */
    public List<Vehicle> getVehiclesByType(Vehicle.VehicleType vehicleType) {
        return vehicleRepository.findByVehicleType(vehicleType);
    }

    /**
     * 根据车牌号查找车辆
     */
    public Optional<Vehicle> getVehicleByPlateNumber(String plateNumber) {
        return vehicleRepository.findByPlateNumber(plateNumber);
    }

    /**
     * 审核车辆
     */
    @Transactional
    public Vehicle approveVehicle(Long vehicleId, Boolean approved, String reviewComment) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("车辆不存在，ID: " + vehicleId));

        // 临时车辆不需要审核
        if (Boolean.TRUE.equals(vehicle.getIsTemporary())) {
            throw new RuntimeException("临时车辆不需要审核");
        }

        if (approved) {
            vehicle.setStatus(Vehicle.VehicleStatus.APPROVED);
        } else {
            vehicle.setStatus(Vehicle.VehicleStatus.REJECTED);
        }

        vehicle.setUpdatedAt(LocalDateTime.now());
        return vehicleRepository.save(vehicle);
    }

    /**
     * 批量审核车辆
     */
    @Transactional
    public List<Vehicle> batchApproveVehicles(List<Long> vehicleIds, Boolean approved) {
        List<Vehicle> vehicles = vehicleRepository.findAllById(vehicleIds);

        for (Vehicle vehicle : vehicles) {
            if (Boolean.TRUE.equals(vehicle.getIsTemporary())) {
                continue; // 跳过临时车辆
            }

            if (approved) {
                vehicle.setStatus(Vehicle.VehicleStatus.APPROVED);
            } else {
                vehicle.setStatus(Vehicle.VehicleStatus.REJECTED);
            }
            vehicle.setUpdatedAt(LocalDateTime.now());
        }

        return vehicleRepository.saveAll(vehicles);
    }

    /**
     * 获取待审核车辆 - 修复版本
     */
    public List<Vehicle> getPendingVehicles() {
        System.out.println("=== VehicleService: 查询待审核车辆 ===");

        try {
            List<User.UserType> campusUserTypes = List.of(
                    User.UserType.STUDENT,
                    User.UserType.TEACHER,
                    User.UserType.STAFF
            );

            List<Vehicle> pendingVehicles = vehicleRepository.findPendingCampusUserVehicles(campusUserTypes);
            System.out.println("✅ 查询到的待审核车辆数量: " + pendingVehicles.size());

            // 🚨 删除下面这些调试代码，它们会导致懒加载异常
            // List<Vehicle> allPending = vehicleRepository.findByStatus(Vehicle.VehicleStatus.PENDING);
            // System.out.println("✅ 方法2 - 所有PENDING状态车辆: " + allPending.size());

            // 🚨 删除打印车辆详情的循环代码

            return pendingVehicles;

        } catch (Exception e) {
            System.err.println("❌ 查询待审核车辆异常: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 获取已审核车辆
     */
    public List<Vehicle> getApprovedVehicles() {
        // 使用参数方式调用
        List<User.UserType> campusUserTypes = List.of(
                User.UserType.STUDENT,
                User.UserType.TEACHER,
                User.UserType.STAFF
        );
        return vehicleRepository.findApprovedCampusUserVehicles(campusUserTypes);
    }

    /**
     * 获取已拒绝车辆
     */
    public List<Vehicle> getRejectedVehicles() {
        return vehicleRepository.findByStatus(Vehicle.VehicleStatus.REJECTED);
    }

    /**
     * 根据用户类型获取车辆
     */
    public List<Vehicle> getVehiclesByUserType(User.UserType userType) {
        return vehicleRepository.findByUserUserType(userType);
    }

    /**
     * 获取校内用户车辆（需要审核的）
     */
    public List<Vehicle> getCampusUserVehicles() {
        List<User.UserType> campusUserTypes = List.of(
                User.UserType.STUDENT,
                User.UserType.TEACHER,
                User.UserType.STAFF
        );
        return vehicleRepository.findByUserUserTypeInAndIsTemporaryFalse(campusUserTypes);
    }

    /**
     * 获取校外用户永久车辆
     */
    public List<Vehicle> getExternalUserPermanentVehicles() {
        return vehicleRepository.findByUserUserTypeAndIsTemporaryFalse(User.UserType.EXTERNAL_USER);
    }

    /**
     * 获取临时车辆
     */
    public List<Vehicle> getTemporaryVehicles() {
        return vehicleRepository.findByIsTemporaryTrue();
    }

    /**
     * 根据状态和用户类型获取车辆
     */
    public List<Vehicle> getVehiclesByStatusAndUserType(Vehicle.VehicleStatus status, User.UserType userType) {
        return vehicleRepository.findByStatusAndUserUserType(status, userType);
    }
    /**
     * 获取所有校内用户的车辆（排除校外用户）
     */
    public List<Vehicle> getCampusUserVehiclesOnly() {
        System.out.println("=== 查询校内用户车辆（排除校外用户）===");

        List<User.UserType> campusUserTypes = List.of(
                User.UserType.STUDENT,
                User.UserType.TEACHER,
                User.UserType.STAFF
        );

        List<Vehicle> allVehicles = vehicleRepository.findAll();

        // 过滤校内用户车辆
        List<Vehicle> campusVehicles = allVehicles.stream()
                .filter(vehicle -> {
                    if (vehicle.getUser() == null) {
                        System.out.println("⚠️ 车辆无用户信息: " + vehicle.getPlateNumber());
                        return false;
                    }
                    User.UserType userType = vehicle.getUser().getUserType();
                    boolean isCampusUser = campusUserTypes.contains(userType);
                    if (isCampusUser) {
                        System.out.println("✅ 校内用户车辆: " + vehicle.getPlateNumber() +
                                " (用户类型: " + userType + ")");
                    } else {
                        System.out.println("❌ 校外用户车辆: " + vehicle.getPlateNumber() +
                                " (用户类型: " + userType + ") - 已过滤");
                    }
                    return isCampusUser;
                })
                .collect(Collectors.toList());

        System.out.println("✅ 校内用户车辆总数: " + campusVehicles.size());
        return campusVehicles;
    }
    /**
     * 统计各类车辆数量
     */
    public VehicleStats getVehicleStatistics() {
        long totalVehicles = vehicleRepository.count();
        long pendingVehicles = vehicleRepository.countByStatus(Vehicle.VehicleStatus.PENDING);
        long approvedVehicles = vehicleRepository.countByStatus(Vehicle.VehicleStatus.APPROVED);
        long rejectedVehicles = vehicleRepository.countByStatus(Vehicle.VehicleStatus.REJECTED);
        long temporaryVehicles = vehicleRepository.countByIsTemporaryTrue();
        long permanentVehicles = vehicleRepository.countByIsTemporaryFalse();

        return new VehicleStats(totalVehicles, pendingVehicles, approvedVehicles,
                rejectedVehicles, temporaryVehicles, permanentVehicles);
    }
    /**
     * 获取所有车辆 - 详细调试版本
     */
    public List<Vehicle> getAllVehicles() {
        System.out.println("=== VehicleService: 查询所有车辆 ===");

        try {
            // 方法1: 使用 findAll()
            System.out.println("🔍 方法1: 使用 vehicleRepository.findAll()");
            List<Vehicle> vehicles = vehicleRepository.findAll();
            System.out.println("✅ findAll() 结果: " + vehicles.size() + " 条记录");

            // 详细打印车辆信息
            if (vehicles.size() > 0) {
                System.out.println("📋 车辆详细信息:");
                for (int i = 0; i < vehicles.size(); i++) {
                    Vehicle vehicle = vehicles.get(i);
                    System.out.println("  车辆 " + (i+1) + ":");
                    System.out.println("    ID: " + vehicle.getId());
                    System.out.println("    车牌: " + vehicle.getPlateNumber());
                    System.out.println("    状态: " + vehicle.getStatus());
                    System.out.println("    是否临时: " + vehicle.getIsTemporary());
                    System.out.println("    用户: " + (vehicle.getUser() != null ?
                            vehicle.getUser().getUsername() + " (" + vehicle.getUser().getRealName() + ")" : "null"));
                    System.out.println("    创建时间: " + vehicle.getCreatedAt());
                }
            } else {
                System.out.println("⚠️ 数据库中没有车辆记录");
            }

            return vehicles;
        } catch (Exception e) {
            System.out.println("❌ 查询所有车辆异常: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 车辆统计信息类
     */
    public static class VehicleStats {
        private final long totalVehicles;
        private final long pendingVehicles;
        private final long approvedVehicles;
        private final long rejectedVehicles;
        private final long temporaryVehicles;
        private final long permanentVehicles;

        public VehicleStats(long totalVehicles, long pendingVehicles, long approvedVehicles,
                            long rejectedVehicles, long temporaryVehicles, long permanentVehicles) {
            this.totalVehicles = totalVehicles;
            this.pendingVehicles = pendingVehicles;
            this.approvedVehicles = approvedVehicles;
            this.rejectedVehicles = rejectedVehicles;
            this.temporaryVehicles = temporaryVehicles;
            this.permanentVehicles = permanentVehicles;
        }

        // Getters
        public long getTotalVehicles() { return totalVehicles; }
        public long getPendingVehicles() { return pendingVehicles; }
        public long getApprovedVehicles() { return approvedVehicles; }
        public long getRejectedVehicles() { return rejectedVehicles; }
        public long getTemporaryVehicles() { return temporaryVehicles; }
        public long getPermanentVehicles() { return permanentVehicles; }
    }

    /**
     * 获取用户车辆数量
     */
    public long getUserVehicleCount(Long userId) {
        return vehicleRepository.countByUserId(userId);
    }

    /**
     * 获取用户特定状态的车辆数量
     */
    public long getUserVehicleCountByStatus(Long userId, Vehicle.VehicleStatus status) {
        return vehicleRepository.countByUserIdAndStatus(userId, status);
    }

    /**
     * 检查用户是否可以添加更多车辆
     */
    public boolean canUserAddMoreVehicles(Long userId) {
        long currentCount = getUserVehicleCount(userId);
        // 假设每个用户最多可以添加5辆车
        return currentCount < 5;
    }

    /**
     * 根据车牌号搜索车辆（模糊匹配）
     */
    public List<Vehicle> searchVehiclesByPlateNumber(String plateNumber) {
        return vehicleRepository.findByPlateNumberContaining(plateNumber);
    }

    /**
     * 根据品牌搜索车辆
     */
    public List<Vehicle> searchVehiclesByBrand(String brand) {
        return vehicleRepository.findByBrandContaining(brand);
    }

    /**
     * 根据用户真实姓名搜索车辆
     */
    public List<Vehicle> searchVehiclesByUserRealName(String realName) {
        return vehicleRepository.findByUserRealNameContaining(realName);
    }
    /**
     * 获取所有车辆DTO（排除校外用户）- 供前端使用
     */
    public List<VehicleDTO> getAllCampusUserVehiclesDTO() {
        List<Vehicle> vehicles = getAllCampusUserVehicles();

        return vehicles.stream().map(vehicle -> {
            VehicleDTO dto = new VehicleDTO();
            dto.setId(vehicle.getId());
            dto.setPlateNumber(vehicle.getPlateNumber());
            dto.setVehicleType(vehicle.getVehicleType().name());
            dto.setBrand(vehicle.getBrand());
            dto.setColor(vehicle.getColor());
            dto.setStatus(vehicle.getStatus().name());
            dto.setIsTemporary(vehicle.getIsTemporary());
            dto.setCreatedAt(vehicle.getCreatedAt());
            dto.setUpdatedAt(vehicle.getUpdatedAt());

            if (vehicle.getUser() != null) {
                dto.setUserId(vehicle.getUser().getId());
                dto.setUserName(vehicle.getUser().getUsername());
                dto.setUserRealName(vehicle.getUser().getRealName());
                dto.setUserType(vehicle.getUser().getUserType().name());
            }

            return dto;
        }).collect(Collectors.toList());
    }
    /**
     * 获取用户临时车辆
     */
    public List<Vehicle> getUserTemporaryVehicles(Long userId) {
        return vehicleRepository.findUserTemporaryVehicles(userId);
    }

    /**
     * 获取用户永久车辆
     */
    public List<Vehicle> getUserPermanentVehicles(Long userId) {
        return vehicleRepository.findUserPermanentVehicles(userId);
    }

    public Vehicle findByPlateNumberAndUserAndStatus(String plateNumber, Long userId, Vehicle.VehicleStatus status) {
        return vehicleRepository.findByPlateNumberAndUserIdAndStatus(plateNumber, userId, status)
                .orElse(null);
    }

    public boolean isVehicleParking(String plateNumber) {
        List<ParkingRecord> activeRecords = parkingRecordRepository.findByPlateNumberAndStatus(
                plateNumber,
                ParkingRecord.ParkingStatus.PARKING
        );
        return !activeRecords.isEmpty();
    }

    // ✅ 修改为：调用 ParkingRecordService 的方法
    public ParkingRecord startCampusParking(Long userId, Long vehicleId, Long spotId) {
        // 委托给 ParkingRecordService
        return parkingRecordService.startParking(userId, vehicleId, spotId);
        // 或者：return parkingRecordService.realTimeStartParking(userId, vehicleId, spotId);
    }
}
