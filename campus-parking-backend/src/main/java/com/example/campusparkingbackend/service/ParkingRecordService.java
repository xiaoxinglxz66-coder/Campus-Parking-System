package com.example.campusparkingbackend.service;

import com.example.campusparkingbackend.controller.ParkingRecordController;
import com.example.campusparkingbackend.entity.ParkingRecord;
import com.example.campusparkingbackend.entity.ParkingSpot;
import com.example.campusparkingbackend.entity.User;
import com.example.campusparkingbackend.entity.Vehicle;
import com.example.campusparkingbackend.repository.ParkingRecordRepository;
import com.example.campusparkingbackend.repository.ParkingSpotRepository;
import com.example.campusparkingbackend.repository.UserRepository;
import com.example.campusparkingbackend.repository.VehicleRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ParkingRecordService {

    @Autowired
    private ParkingRecordRepository parkingRecordRepository;

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    // 基础费率配置
    private static final BigDecimal HOURLY_RATE = new BigDecimal("5.00");
    private static final BigDecimal DAILY_MAX = new BigDecimal("50.00");

    // ========== 实时停车方法 ==========

    /**
     * 实时开始停车 - 优化版本
     */
    @Transactional
    public ParkingRecord realTimeStartParking(Long userId, Long vehicleId, Long spotId) {
        try {

            // 验证基础信息
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            Vehicle vehicle = vehicleRepository.findById(vehicleId)
                    .orElseThrow(() -> new RuntimeException("车辆不存在"));

            ParkingSpot spot = parkingSpotRepository.findById(spotId)
                    .orElseThrow(() -> new RuntimeException("停车位不存在"));

            // 实时状态验证
            validateRealTimeParkingConditions(userId, vehicle, spot);

            // 创建实时停车记录
            ParkingRecord record = createRealTimeParkingRecord(user, vehicle, spot);
            System.out.println("  停车记录设置的区域: " + record.getParkingLotName());
            // 实时更新停车位状态
            updateSpotStatusRealTime(spot, ParkingSpot.SpotStatus.OCCUPIED);
// 关键：检查停车位的zone字段
            System.out.println("⚠️ DEBUG - 停车位区域检查:");
            System.out.println("  车位ID: " + spotId);
            System.out.println("  车位编号: " + spot.getSpotNumber());
            System.out.println("  区域(zone): " + spot.getZone());
            System.out.println("  是否为科大讯飞楼: " + spot.getZone().contains("科大"));
            System.out.println("🅿️ 实时停车开始 - 用户: " + user.getRealName() +
                    ", 车牌: " + vehicle.getPlateNumber() +
                    ", 车位: " + spot.getSpotNumber());

            return parkingRecordRepository.save(record);

        } catch (Exception e) {
            System.err.println("❌ 实时停车开始失败: " + e.getMessage());
            throw new RuntimeException("停车开始失败: " + e.getMessage());
        }
    }

    /**
     * 实时结束停车 - 优化版本
     */
    @Transactional
    public ParkingRecord realTimeEndParking(Long recordId, Long userId) {
        try {
            ParkingRecord record = parkingRecordRepository.findById(recordId)
                    .orElseThrow(() -> new RuntimeException("停车记录不存在"));

            // 实时权限验证
            validateRealTimeOperationPermission(record, userId);

            // 实时状态检查
            if (record.getStatus() != ParkingRecord.ParkingStatus.PARKING) {
                throw new RuntimeException("该停车记录已结束");
            }

            // 设置结束时间并计算费用
            record.setEndTime(LocalDateTime.now());
            record.setStatus(ParkingRecord.ParkingStatus.COMPLETED);

            // 实时计算费用
            BigDecimal fee = calculateRealTimeParkingFee(record);
            record.setFee(fee);

            // 实时释放停车位
            updateSpotStatusRealTime(record.getParkingSpot(), ParkingSpot.SpotStatus.AVAILABLE);

            System.out.println("🅿️ 实时停车结束 - 记录ID: " + recordId +
                    ", 费用: " + fee + "元, 时长: " +
                    record.calculateDurationInMinutes() + "分钟");

            return parkingRecordRepository.save(record);

        } catch (Exception e) {
            System.err.println("❌ 实时停车结束失败: " + e.getMessage());
            throw new RuntimeException("停车结束失败: " + e.getMessage());
        }
    }

    /**
     * 实时验证停车条件
     */
    private void validateRealTimeParkingConditions(Long userId, Vehicle vehicle, ParkingSpot spot) {
        // 验证车辆权限
        if (!vehicle.getUser().getId().equals(userId)) {
            throw new RuntimeException("车辆不属于当前用户");
        }

        // 验证车辆状态
        if (vehicle.getStatus() != Vehicle.VehicleStatus.APPROVED) {
            throw new RuntimeException("车辆未通过审核");
        }

        // 验证停车位实时状态
        if (spot.getStatus() != ParkingSpot.SpotStatus.AVAILABLE) {
            throw new RuntimeException("停车位已被占用");
        }

        // 验证用户是否有进行中的停车记录
        List<ParkingRecord> activeRecords = parkingRecordRepository.findByUserIdAndStatus(
                userId, ParkingRecord.ParkingStatus.PARKING);
        if (!activeRecords.isEmpty()) {
            throw new RuntimeException("您有未结束的停车记录");
        }
    }

    /**
     * 创建实时停车记录
     */
    private ParkingRecord createRealTimeParkingRecord(User user, Vehicle vehicle, ParkingSpot spot) {
        ParkingRecord record = new ParkingRecord(user, vehicle, spot);
        record.setStartTime(LocalDateTime.now());
        record.setStatus(ParkingRecord.ParkingStatus.PARKING);
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        return record;
    }

    /**
     * 实时更新停车位状态
     */
    private void updateSpotStatusRealTime(ParkingSpot spot, ParkingSpot.SpotStatus status) {
        spot.setStatus(status);
        parkingSpotRepository.save(spot);
    }

    /**
     * 实时计算停车费用
     */
    private BigDecimal calculateRealTimeParkingFee(ParkingRecord record) {
        // 校内用户免费
        if (record.getUser().getUserType() != User.UserType.EXTERNAL_USER) {
            return BigDecimal.ZERO;
        }

        LocalDateTime endTime = record.getEndTime() != null ? record.getEndTime() : LocalDateTime.now();
        Duration duration = Duration.between(record.getStartTime(), endTime);

        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;

        // 实时计费规则：不足1小时按1小时计算
        if (minutes > 0) {
            hours++;
        }
        hours = Math.max(1, hours); // 至少1小时

        BigDecimal totalFee = HOURLY_RATE.multiply(BigDecimal.valueOf(hours));

        // 实时费用封顶
        return totalFee.min(DAILY_MAX);
    }

    /**
     * 实时权限验证
     */
    private void validateRealTimeOperationPermission(ParkingRecord record, Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        boolean isAdmin = currentUser.getUserType() == User.UserType.ADMIN;
        boolean isOwner = record.getUser().getId().equals(userId);

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("无权操作此停车记录");
        }
    }

    /**
     * 获取实时停车数据
     */
    public RealTimeParkingData getRealTimeParkingData() {
        long totalTodayRecords = parkingRecordRepository.countRealTimeTodayRecords();
        long activeParkingCount = parkingRecordRepository.countByStatus(ParkingRecord.ParkingStatus.PARKING);
        BigDecimal todayRevenue = parkingRecordRepository.calculateRealTimeTodayRevenue();
        List<Object[]> lotOccupancy = parkingRecordRepository.countRealTimeParkingByLot();

        return new RealTimeParkingData(totalTodayRecords, activeParkingCount, todayRevenue, lotOccupancy);
    }

    /**
     * 获取实时停车记录（今日及以后）
     */
    public List<ParkingRecord> getRealTimeParkingRecords() {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return parkingRecordRepository.findRealTimeRecords(todayStart);
    }

    // ========== 分页查询方法 ==========

    /**
     * 获取所有停车记录（支持分页和筛选）
     */
    @Transactional
    public Page<ParkingRecord> getAllParkingRecordsWithFilters(
            int page, int size, String parkingLot, String status,
            String userType, String search) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));
            Specification<ParkingRecord> spec = buildParkingRecordSpecification(parkingLot, status, userType, search);

            // 使用JOIN FETCH确保用户信息被加载
            Page<ParkingRecord> result = parkingRecordRepository.findAll(spec, pageable);

            // 在事务内修复数据
            result.getContent().forEach(record -> {
                // 如果user对象已加载（通过JOIN FETCH），可以安全访问
                if (record.getUser() != null) {
                    User user = record.getUser();

                    // 修复校外用户的显示
                    if (user.getUserType() == User.UserType.EXTERNAL_USER) {
                        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
                            record.setUserName(user.getPhone()); // 设置为电话号码
                        } else if (record.getUserName() == null || record.getUserName().isEmpty() || "未知用户".equals(record.getUserName())) {
                            record.setUserName(user.getUsername() != null ? user.getUsername() : "校外用户");
                        }
                    } else if (record.getUserName() == null || record.getUserName().isEmpty() || "未知用户".equals(record.getUserName())) {
                        // 校内用户
                        if (user.getRealName() != null && !user.getRealName().trim().isEmpty()) {
                            record.setUserName(user.getRealName());
                        } else {
                            record.setUserName(user.getUsername() != null ? user.getUsername() : "校内用户");
                        }
                    }

                    // 确保用户类型正确
                    record.setUserType(user.getUserType().name());
                }
            });

            return result;

        } catch (Exception e) {
            throw new RuntimeException("查询停车记录失败: " + e.getMessage());
        }
    }

    /**
     * 增强停车记录信息 - 确保用户名和用户类型正确
     */
    private void enhanceParkingRecordInfo(ParkingRecord record) {
        if (record == null) return;

        try {
            // 确保用户信息已加载（因为使用了LAZY加载）
            if (record.getUser() != null) {
                User user = record.getUser();

                // 根据用户类型设置正确的用户名
                if (user.getUserType() == User.UserType.EXTERNAL_USER) {
                    // 校外用户：使用电话号码作为用户名
                    String phone = user.getPhone();
                    if (phone != null && !phone.trim().isEmpty()) {
                        record.setUserName(phone);
                    } else {
                        // 如果没有电话号码，使用用户名
                        record.setUserName(user.getUsername() != null ? user.getUsername() : "校外用户");
                    }
                } else {
                    // 校内用户：使用真实姓名
                    String realName = user.getRealName();
                    if (realName != null && !realName.trim().isEmpty()) {
                        record.setUserName(realName);
                    } else {
                        record.setUserName(user.getUsername() != null ? user.getUsername() : "校内用户");
                    }
                }

                // 确保用户类型正确设置
                record.setUserType(user.getUserType().name());
            }
        } catch (Exception e) {
            System.err.println("⚠️ 增强停车记录信息失败: " + e.getMessage());
            // 如果发生异常，至少确保有默认值
            if (record.getUserName() == null || record.getUserName().isEmpty()) {
                record.setUserName("未知用户");
            }
            if (record.getUserType() == null || record.getUserType().isEmpty()) {
                record.setUserType("UNKNOWN");
            }
        }
    }

    /**
     * 构建停车记录查询条件
     */
    private Specification<ParkingRecord> buildParkingRecordSpecification(
            String parkingLot, String status, String userType, String search) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            System.out.println("🔧 构建查询条件...");

            // 停车场筛选
            if (parkingLot != null && !parkingLot.trim().isEmpty()) {
                System.out.println("✅ 添加停车场条件: " + parkingLot);
                predicates.add(criteriaBuilder.equal(root.get("parkingLotName"), parkingLot.trim()));
            }

            // 状态筛选
            if (status != null && !status.trim().isEmpty()) {
                try {
                    ParkingRecord.ParkingStatus parkingStatus =
                            ParkingRecord.ParkingStatus.valueOf(status.trim().toUpperCase());
                    System.out.println("✅ 添加状态条件: " + status);
                    predicates.add(criteriaBuilder.equal(root.get("status"), parkingStatus));
                } catch (IllegalArgumentException e) {
                    // 如果状态参数无效，忽略该条件
                    System.err.println("⚠️ 无效的状态参数: " + status);
                }
            }

            // 用户类型筛选
            if (userType != null && !userType.trim().isEmpty()) {
                if ("CAMPUS_USER".equals(userType.trim())) {
                    // 校内用户：STUDENT, TEACHER, STAFF
                    System.out.println("✅ 添加校内用户条件");
                    predicates.add(criteriaBuilder.in(root.get("userType")).value("STUDENT").value("TEACHER").value("STAFF"));
                } else {
                    System.out.println("✅ 添加用户类型条件: " + userType);
                    predicates.add(criteriaBuilder.equal(root.get("userType"), userType.trim()));
                }
            }

            // 搜索条件（车牌号或用户名）
            if (search != null && !search.trim().isEmpty()) {
                System.out.println("✅ 添加搜索条件: " + search);
                String searchPattern = "%" + search.trim() + "%";
                Predicate plateNumberPredicate = criteriaBuilder.like(root.get("plateNumber"), searchPattern);
                Predicate userNamePredicate = criteriaBuilder.like(root.get("userName"), searchPattern);
                predicates.add(criteriaBuilder.or(plateNumberPredicate, userNamePredicate));
            }

            if (!predicates.isEmpty()) {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }

            System.out.println("ℹ️ 无筛选条件，返回所有记录");
            return criteriaBuilder.conjunction(); // 返回恒真条件
        };
    }

    /**
     * 分页查询校外用户收费记录 - 修复废弃API
     */
    public Page<ParkingRecord> getExternalUserRecordsWithPagination(String plateNumber, LocalDateTime startDate,
                                                                    LocalDateTime endDate, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));

        // 修复：使用新的方式构建查询条件
        Specification<ParkingRecord> spec = buildExternalUserSpecification(plateNumber, startDate, endDate, status);

        return parkingRecordRepository.findAll(spec, pageable);
    }

    /**
     * 构建校外用户查询条件 - 修复废弃API
     */
    private Specification<ParkingRecord> buildExternalUserSpecification(
            String plateNumber, LocalDateTime startDate, LocalDateTime endDate, String status) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 基本条件：用户类型为校外用户
            predicates.add(criteriaBuilder.equal(root.get("userType"), "EXTERNAL_USER"));

            // 车牌号筛选
            if (plateNumber != null && !plateNumber.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("plateNumber"), "%" + plateNumber + "%"));
            }

            // 开始时间筛选
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), startDate));
            }

            // 结束时间筛选
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), endDate));
            }

            // 状态筛选
            if (status != null && !status.trim().isEmpty()) {
                try {
                    ParkingRecord.ParkingStatus parkingStatus = ParkingRecord.ParkingStatus.valueOf(status.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("status"), parkingStatus));
                } catch (IllegalArgumentException e) {
                    // 如果状态参数无效，忽略该条件
                    System.err.println("⚠️ 无效的状态参数: " + status);
                }
            }

            if (!predicates.isEmpty()) {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
            return criteriaBuilder.conjunction(); // 返回恒真条件
        };
    }

    // ========== 原有方法（保持兼容性）==========

    /**
     * 开始停车（兼容旧版本）
     */
    @Transactional
    public ParkingRecord startParking(Long userId, Long vehicleId, Long spotId) {
        return realTimeStartParking(userId, vehicleId, spotId);
    }

    /**
     * 结束停车（兼容旧版本）
     */
    @Transactional
    public ParkingRecord endParking(Long recordId, Long userId) {
        return realTimeEndParking(recordId, userId);
    }

    /**
     * 结束停车并计算费用（使用用户名）
     */
    @Transactional
    public ParkingRecord endParkingWithUsername(Long recordId, String username) {
        // 通过用户名查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return endParking(recordId, user.getId());
    }

    /**
     * 计算停车费用
     */
    private BigDecimal calculateParkingFee(LocalDateTime startTime, LocalDateTime endTime, User.UserType userType) {
        // 校内用户免费
        if (userType != User.UserType.EXTERNAL_USER) {
            return BigDecimal.ZERO;
        }

        Duration duration = Duration.between(startTime, endTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;

        // 不足1小时按1小时计算
        if (minutes > 0) {
            hours++;
        }

        // 至少1小时
        hours = Math.max(1, hours);

        BigDecimal totalFee = HOURLY_RATE.multiply(BigDecimal.valueOf(hours));

        // 应用每日封顶
        if (totalFee.compareTo(DAILY_MAX) > 0) {
            return DAILY_MAX;
        }

        return totalFee;
    }

    /**
     * 取消停车记录
     */
    @Transactional
    public ParkingRecord cancelParking(Long recordId, Long userId) {
        ParkingRecord record = parkingRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("停车记录不存在"));

        if (!record.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作此停车记录");
        }

        if (record.getStatus() != ParkingRecord.ParkingStatus.PARKING) {
            throw new RuntimeException("只能取消进行中的停车记录");
        }

        record.setStatus(ParkingRecord.ParkingStatus.CANCELLED);
        record.setUpdatedAt(LocalDateTime.now());

        // 释放停车位
        ParkingSpot spot = record.getParkingSpot();
        spot.setStatus(ParkingSpot.SpotStatus.AVAILABLE);
        parkingSpotRepository.save(spot);

        return parkingRecordRepository.save(record);
    }

    /**
     * 取消停车记录（使用用户名）
     */
    @Transactional
    public ParkingRecord cancelParkingWithUsername(Long recordId, String username) {
        // 通过用户名查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return cancelParking(recordId, user.getId());
    }
    // 在 ParkingRecordService.java 中添加此方法
    @Transactional
    public void fixExternalUserNames() {
        System.out.println("🔄 开始修复校外用户停车记录的用户名...");

        try {
            // 获取所有校外用户的停车记录
            List<ParkingRecord> externalRecords = parkingRecordRepository.findByUserType("EXTERNAL_USER");

            int fixedCount = 0;
            for (ParkingRecord record : externalRecords) {
                if (record.getUser() != null) {
                    User user = record.getUser();

                    // 检查当前用户名是否需要修复
                    String currentName = record.getUserName();
                    boolean needsFix = currentName == null ||
                            currentName.isEmpty() ||
                            "未知用户".equals(currentName) ||
                            (user.getPhone() != null && !currentName.equals(user.getPhone()));

                    if (needsFix && user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
                        // 使用电话号码作为用户名
                        record.setUserName(user.getPhone());
                        fixedCount++;
                    }
                }
            }

            // 批量保存修复的记录
            if (fixedCount > 0) {
                parkingRecordRepository.saveAll(externalRecords);
                System.out.println("✅ 成功修复 " + fixedCount + " 条校外用户停车记录的用户名");
            } else {
                System.out.println("ℹ️ 没有需要修复的校外用户停车记录");
            }

        } catch (Exception e) {
            System.err.println("❌ 修复校外用户停车记录用户名失败: " + e.getMessage());
            throw new RuntimeException("修复数据失败: " + e.getMessage());
        }
    }
    /**
     * 校外用户临时停车
     */
    @Transactional
    public ParkingRecord externalTempParking(Long userId, String plateNumber, Long spotId) {
        try {
            // 检查用户是否存在
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户信息不存在，请重新登录"));

            // 检查停车位是否存在且可用
            ParkingSpot spot = parkingSpotRepository.findById(spotId)
                    .orElseThrow(() -> new RuntimeException("选择的停车位不存在"));

            if (spot.getStatus() != ParkingSpot.SpotStatus.AVAILABLE) {
                throw new RuntimeException("该停车位已被占用，请选择其他车位");
            }

            // 检查用户是否已经有进行中的停车记录
            List<ParkingRecord> currentParking = parkingRecordRepository.findByUserIdAndStatus(userId, ParkingRecord.ParkingStatus.PARKING);
            if (!currentParking.isEmpty()) {
                throw new RuntimeException("您当前已有车辆在停车中，请先结束当前停车");
            }

            // 检查该车牌号是否正在停车中
            List<ParkingRecord> existingParkingRecords = parkingRecordRepository.findByPlateNumberAndStatus(plateNumber, ParkingRecord.ParkingStatus.PARKING);
            if (!existingParkingRecords.isEmpty()) {
                throw new RuntimeException("该车牌号当前正在停车中，请确认车牌号是否正确");
            }

            // 修复：查找或创建临时车辆
            Vehicle vehicle = findOrCreateTempVehicle(user, plateNumber);

            // ========== 使用修改后的构造函数 ==========
            // 注意：这里假设你已经按照我之前建议修改了ParkingRecord的构造函数
            // 构造函数会自动根据用户类型设置正确的用户名
            ParkingRecord record = new ParkingRecord(user, plateNumber, spot);
            record.setStartTime(LocalDateTime.now());
            record.setStatus(ParkingRecord.ParkingStatus.PARKING);

            // 但为了安全，我们还是明确设置一下用户名
            // 这是因为你可能还没有修改构造函数，或者构造函数逻辑有问题
            if (user.getUserType() == User.UserType.EXTERNAL_USER) {
                if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
                    record.setUserName(user.getPhone());
                }
            }
            // ========== 修改结束 ==========

            // 更新停车位状态
            spot.setStatus(ParkingSpot.SpotStatus.OCCUPIED);
            parkingSpotRepository.save(spot);

            return parkingRecordRepository.save(record);

        } catch (Exception e) {
            System.err.println("❌ 临时停车失败: " + e.getMessage());
            throw new RuntimeException("停车失败: " + e.getMessage());
        }
    }

    private Vehicle findOrCreateTempVehicle(User user, String plateNumber) {
        try {
            // 1. 首先尝试查找该车牌号的临时车辆
            Optional<Vehicle> existingVehicle = vehicleRepository.findByPlateNumber(plateNumber);

            if (existingVehicle.isPresent()) {
                Vehicle vehicle = existingVehicle.get();

                // 如果是临时车辆，直接返回使用
                if (Boolean.TRUE.equals(vehicle.getIsTemporary())) {
                    System.out.println("✅ 找到现有临时车辆: " + plateNumber);
                    return vehicle;
                } else {
                    // 如果是永久车辆，生成新的临时车牌号
                    String newPlateNumber = generateTempPlateNumber(plateNumber);
                    System.out.println("🔄 永久车辆存在，生成新临时车牌: " + newPlateNumber);
                    return createNewTempVehicle(user, newPlateNumber);
                }
            }

            // 2. 没有找到，创建新的临时车辆
            System.out.println("🆕 创建新临时车辆: " + plateNumber);
            return createNewTempVehicle(user, plateNumber);

        } catch (Exception e) {
            System.err.println("❌ 创建临时车辆失败: " + e.getMessage());
            // 如果还是失败，生成唯一车牌号
            String uniquePlateNumber = generateUniqueTempPlateNumber(plateNumber);
            return createNewTempVehicle(user, uniquePlateNumber);
        }
    }

    /**
     * 生成临时车牌号（避免与永久车辆冲突）
     */
    private String generateTempPlateNumber(String originalPlate) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String suffix = timestamp.substring(timestamp.length() - 4);
        return originalPlate + "_T" + suffix;
    }

    /**
     * 生成唯一临时车牌号（处理所有冲突情况）
     */
    private String generateUniqueTempPlateNumber(String originalPlate) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomSuffix = String.valueOf((int)(Math.random() * 1000));
        return originalPlate + "_TEMP_" + timestamp + "_" + randomSuffix;
    }

    /**
     * 创建新的临时车辆
     */
    private Vehicle createNewTempVehicle(User user, String plateNumber) {
        Vehicle vehicle = new Vehicle();
        vehicle.setPlateNumber(plateNumber);
        vehicle.setVehicleType(Vehicle.VehicleType.CAR);
        vehicle.setBrand("临时车辆");
        vehicle.setColor("未知");
        vehicle.setUser(user);
        vehicle.setStatus(Vehicle.VehicleStatus.APPROVED);
        vehicle.setIsTemporary(true);
        vehicle.setCreatedAt(LocalDateTime.now());
        vehicle.setUpdatedAt(LocalDateTime.now());

        return vehicleRepository.save(vehicle);
    }

    // ========== 财务管理方法 ==========

    /**
     * 获取财务概览数据
     */
    public ParkingRecordController.FinanceOverviewData getFinanceOverview() {
        // 今日收入
        BigDecimal todayRevenue = parkingRecordRepository.calculateTodayRevenue();
        if (todayRevenue == null) todayRevenue = BigDecimal.ZERO;

        // 本月收入
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        BigDecimal monthRevenue = parkingRecordRepository.calculateExternalUserRevenueByDateRange(monthStart, monthEnd);
        if (monthRevenue == null) monthRevenue = BigDecimal.ZERO;

        // 年度收入
        LocalDateTime yearStart = LocalDateTime.now().withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime yearEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        BigDecimal yearRevenue = parkingRecordRepository.calculateExternalUserRevenueByDateRange(yearStart, yearEnd);
        if (yearRevenue == null) yearRevenue = BigDecimal.ZERO;

        // 今日停车数量
        long todayParkingCount = parkingRecordRepository.countTodayParkingRecords();

        // 当前停车数量
        long currentParkingCount = parkingRecordRepository.countByStatus(ParkingRecord.ParkingStatus.PARKING);

        return new ParkingRecordController.FinanceOverviewData(todayRevenue, monthRevenue, yearRevenue, todayParkingCount, currentParkingCount);
    }

    /**
     * 获取收入趋势数据（12个月）
     */
    public List<ParkingRecordController.MonthlyRevenue> getRevenueTrend() {
        List<ParkingRecordController.MonthlyRevenue> monthlyRevenues = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        // 前10个月的固定数据（1月-10月）
        BigDecimal[] fixedRevenues = {
                new BigDecimal("3200.00"), new BigDecimal("2800.00"), new BigDecimal("3500.00"),
                new BigDecimal("4200.00"), new BigDecimal("3800.00"), new BigDecimal("4500.00"),
                new BigDecimal("5200.00"), new BigDecimal("4800.00"), new BigDecimal("5500.00"),
                new BigDecimal("5000.00")
        };

        for (int i = 0; i < 12; i++) {
            int monthOffset = i - 11; // 从11个月前开始
            LocalDateTime monthDate = now.plusMonths(monthOffset);
            int year = monthDate.getYear();
            int month = monthDate.getMonthValue();

            String monthKey = year + "-" + String.format("%02d", month);

            BigDecimal revenue;
            boolean isRealTime;

            if (i < 10) {
                // 前10个月使用固定数据
                revenue = fixedRevenues[i];
                isRealTime = false;
            } else {
                // 11月、12月使用实时数据
                revenue = calculateMonthlyRevenue(year, month);
                isRealTime = true;
            }

            monthlyRevenues.add(new ParkingRecordController.MonthlyRevenue(monthKey, revenue, isRealTime));
        }

        return monthlyRevenues;
    }
// 在 ParkingRecordService.java 中添加以下方法

    /**
     * 检查车辆是否在停车中（通过车牌号）
     */
    public boolean isVehicleParking(String plateNumber) {
        try {
            List<ParkingRecord> activeRecords = parkingRecordRepository.findByPlateNumberAndStatus(
                    plateNumber,
                    ParkingRecord.ParkingStatus.PARKING
            );
            return !activeRecords.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查车辆是否在停车中（通过车辆ID）
     */
    public boolean isVehicleParking(Long vehicleId) {
        try {
            List<ParkingRecord> activeRecords = parkingRecordRepository.findByVehicleIdAndStatus(
                    vehicleId,
                    ParkingRecord.ParkingStatus.PARKING
            );
            return !activeRecords.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * 计算指定月份的实际收入
     */
    private BigDecimal calculateMonthlyRevenue(int year, int month) {
        LocalDateTime monthStart = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

        BigDecimal revenue = parkingRecordRepository.calculateExternalUserRevenueByDateRange(monthStart, monthEnd);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    // ========== 查询方法 ==========
    public Page<ParkingRecord> getUserParkingRecords(Long userId, int page, int size, String parkingLot, String status, String search) {
        // 简化实现
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));
        return parkingRecordRepository.findByUserId(userId, pageable);
    }

    public List<ParkingRecord> getUserParkingRecordsByStatus(Long userId, ParkingRecord.ParkingStatus status) {
        return parkingRecordRepository.findByUserIdAndStatus(userId, status);
    }
    public List<ParkingRecord> getUserParkingRecords(Long userId) {
        return parkingRecordRepository.findByUserId(userId);
    }

    public List<ParkingRecord> getUserCurrentParking(Long userId) {
        return parkingRecordRepository.findByUserIdAndStatus(userId, ParkingRecord.ParkingStatus.PARKING);
    }

    public List<ParkingRecord> getAllParkingRecords() {
        return getRealTimeParkingRecords(); // 只返回实时记录
    }

    public List<ParkingRecord> getParkingRecordsByStatus(ParkingRecord.ParkingStatus status) {
        return parkingRecordRepository.findByStatus(status);
    }

    public ParkingRecord getParkingRecordById(Long recordId) {
        return parkingRecordRepository.findById(recordId).orElse(null);
    }

    public List<ParkingRecord> getParkingRecordsByVehicleAndUser(Long vehicleId, Long userId) {
        List<ParkingRecord> records = parkingRecordRepository.findByVehicleId(vehicleId);
        return records.stream()
                .filter(record -> record.getUser().getId().equals(userId))
                .toList();
    }

    public List<ParkingRecord> getParkingRecordsBySpot(Long spotId) {
        return parkingRecordRepository.findByParkingSpotId(spotId);
    }

    public List<ParkingRecord> getTodayParkingRecords() {
        return parkingRecordRepository.findByStartTimeBetween(
                LocalDateTime.now().withHour(0).withMinute(0).withSecond(0),
                LocalDateTime.now().withHour(23).withMinute(59).withSecond(59)
        );
    }

    public List<ParkingRecord> getParkingRecordsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return parkingRecordRepository.findByStartTimeBetween(startTime, endTime);
    }

    public List<ParkingRecord> getParkingRecordsByParkingLot(String parkingLotName) {
        return parkingRecordRepository.findByParkingLotName(parkingLotName);
    }

    public List<ParkingRecord> getCampusUserParkingRecords() {
        return parkingRecordRepository.findCampusUserRecords();
    }

    public List<ParkingRecord> getExternalUserParkingRecords() {
        return parkingRecordRepository.findExternalUserRecords();
    }

    public List<String> getAllParkingLotNames() {
        return parkingRecordRepository.findAllParkingLotNames();
    }

    public List<ParkingRecord> getUserParkingRecordsByLot(Long userId, String parkingLotName) {
        return parkingRecordRepository.findByUserIdAndParkingLotName(userId, parkingLotName);
    }

    public List<ParkingRecord> getExternalUserParkingRecords(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));
        Page<ParkingRecord> recordsPage = parkingRecordRepository.findByUserId(userId, pageable);
        return recordsPage.getContent();
    }

    public List<ParkingSpot> getAvailableSpots() {
        return parkingSpotRepository.findByStatus(ParkingSpot.SpotStatus.AVAILABLE);
    }

    // ========== 统计方法 ==========

    public ParkingRecordStatistics getParkingStatistics() {
        long totalRecords = parkingRecordRepository.countRealTimeTodayRecords();
        long parkingCount = parkingRecordRepository.countByStatus(ParkingRecord.ParkingStatus.PARKING);
        long completedCount = parkingRecordRepository.countByStatus(ParkingRecord.ParkingStatus.COMPLETED);
        long cancelledCount = parkingRecordRepository.countByStatus(ParkingRecord.ParkingStatus.CANCELLED);

        BigDecimal totalRevenue = parkingRecordRepository.calculateRealTimeTodayRevenue();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        return new ParkingRecordStatistics(totalRecords, parkingCount, completedCount, cancelledCount, totalRevenue);
    }

    public ExternalUserStatistics getExternalUserStatistics(Long userId) {
        List<ParkingRecord> userRecords = parkingRecordRepository.findByUserId(userId);

        long totalRecords = userRecords.size();
        long currentParking = userRecords.stream()
                .filter(record -> record.getStatus() == ParkingRecord.ParkingStatus.PARKING)
                .count();

        BigDecimal totalSpent = userRecords.stream()
                .filter(record -> record.getStatus() == ParkingRecord.ParkingStatus.COMPLETED && record.getFee() != null)
                .map(ParkingRecord::getFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ExternalUserStatistics(totalRecords, currentParking, totalSpent);
    }

    // ========== 统计类定义 ==========

    /**
     * 实时停车数据类
     */
    public static class RealTimeParkingData {
        private final long totalTodayRecords;
        private final long activeParkingCount;
        private final BigDecimal todayRevenue;
        private final List<Object[]> lotOccupancy;

        public RealTimeParkingData(long totalTodayRecords, long activeParkingCount,
                                   BigDecimal todayRevenue, List<Object[]> lotOccupancy) {
            this.totalTodayRecords = totalTodayRecords;
            this.activeParkingCount = activeParkingCount;
            this.todayRevenue = todayRevenue;
            this.lotOccupancy = lotOccupancy;
        }

        // Getters
        public long getTotalTodayRecords() { return totalTodayRecords; }
        public long getActiveParkingCount() { return activeParkingCount; }
        public BigDecimal getTodayRevenue() { return todayRevenue; }
        public List<Object[]> getLotOccupancy() { return lotOccupancy; }
    }

    /**
     * 停车记录统计类
     */
    public static class ParkingRecordStatistics {
        private final long totalRecords;
        private final long parkingCount;
        private final long completedCount;
        private final long cancelledCount;
        private final BigDecimal totalRevenue;

        public ParkingRecordStatistics(long totalRecords, long parkingCount, long completedCount,
                                       long cancelledCount, BigDecimal totalRevenue) {
            this.totalRecords = totalRecords;
            this.parkingCount = parkingCount;
            this.completedCount = completedCount;
            this.cancelledCount = cancelledCount;
            this.totalRevenue = totalRevenue;
        }

        // Getters
        public long getTotalRecords() { return totalRecords; }
        public long getParkingCount() { return parkingCount; }
        public long getCompletedCount() { return completedCount; }
        public long getCancelledCount() { return cancelledCount; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
    }

    /**
     * 校外用户统计类
     */
    public static class ExternalUserStatistics {
        private final long totalRecords;
        private final long currentParking;
        private final BigDecimal totalSpent;

        public ExternalUserStatistics(long totalRecords, long currentParking, BigDecimal totalSpent) {
            this.totalRecords = totalRecords;
            this.currentParking = currentParking;
            this.totalSpent = totalSpent;
        }

        // getters
        public long getTotalRecords() { return totalRecords; }
        public long getCurrentParking() { return currentParking; }
        public BigDecimal getTotalSpent() { return totalSpent; }
    }
}
