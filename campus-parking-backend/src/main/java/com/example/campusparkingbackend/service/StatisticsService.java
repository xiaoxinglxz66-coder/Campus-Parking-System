// src/main/java/com/example/campusparkingbackend/service/StatisticsService.java
package com.example.campusparkingbackend.service;

import com.example.campusparkingbackend.entity.ParkingRecord;
import com.example.campusparkingbackend.entity.User;
import com.example.campusparkingbackend.entity.Vehicle;
import com.example.campusparkingbackend.repository.ParkingRecordRepository;
import com.example.campusparkingbackend.repository.UserRepository;
import com.example.campusparkingbackend.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ParkingRecordRepository parkingRecordRepository;

    /**
     * 获取系统总体统计
     */
    public SystemStatistics getSystemStatistics() {
        // 用户统计
        long totalUsers = userRepository.count();
        long pendingUsers = userRepository.countByUserStatus(User.UserStatus.PENDING);
        long approvedUsers = userRepository.countByUserStatus(User.UserStatus.APPROVED);

        // 车辆统计
        long totalVehicles = vehicleRepository.count();
        long pendingVehicles = vehicleRepository.countByStatus(Vehicle.VehicleStatus.PENDING);
        long approvedVehicles = vehicleRepository.countByStatus(Vehicle.VehicleStatus.APPROVED);

        // 停车记录统计
        long totalRecords = parkingRecordRepository.count();
        long parkingCount = parkingRecordRepository.countByStatus(ParkingRecord.ParkingStatus.PARKING);
        long completedCount = parkingRecordRepository.countByStatus(ParkingRecord.ParkingStatus.COMPLETED);

        // 收入统计
        BigDecimal totalRevenue = parkingRecordRepository.calculateTotalRevenue();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        // 今日收入
        BigDecimal todayRevenue = parkingRecordRepository.calculateTodayRevenue();
        if (todayRevenue == null) {
            todayRevenue = BigDecimal.ZERO;
        }

        return new SystemStatistics(
                totalUsers, pendingUsers, approvedUsers,
                totalVehicles, pendingVehicles, approvedVehicles,
                totalRecords, parkingCount, completedCount,
                totalRevenue, todayRevenue
        );
    }

    /**
     * 获取今日统计
     */
    public TodayStatistics getTodayStatistics() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        // 今日新增用户（需要 UserRepository 中有这个方法）
        long todayNewUsers = 0;
        try {
            // 如果 UserRepository 没有 findByCreatedAtBetween 方法，暂时设为0
            todayNewUsers = userRepository.findByCreatedAtBetween(startOfDay, endOfDay).size();
        } catch (Exception e) {
            todayNewUsers = 0;
        }

        // 今日新增车辆（需要 VehicleRepository 中有这个方法）
        long todayNewVehicles = 0;
        try {
            // 如果 VehicleRepository 没有 findByCreatedAtBetween 方法，暂时设为0
            todayNewVehicles = vehicleRepository.findByCreatedAtBetween(startOfDay, endOfDay).size();
        } catch (Exception e) {
            todayNewVehicles = 0;
        }

        // 今日停车记录
        List<ParkingRecord> todayRecords = parkingRecordRepository.findByStartTimeBetween(startOfDay, endOfDay);
        long todayParkingCount = todayRecords.size();

        // 今日收入
        BigDecimal todayRevenue = todayRecords.stream()
                .filter(record -> record.getFee() != null)
                .map(ParkingRecord::getFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TodayStatistics(
                todayNewUsers, todayNewVehicles, todayParkingCount, todayRevenue
        );
    }

    /**
     * 获取收入统计
     */
    public RevenueStatistics getRevenueStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 获取时间范围内的停车记录
        List<ParkingRecord> records = parkingRecordRepository.findByStartTimeBetween(startDateTime, endDateTime);

        // 计算总收入
        BigDecimal totalRevenue = records.stream()
                .filter(record -> record.getFee() != null)
                .map(ParkingRecord::getFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 按日期分组统计
        Map<LocalDate, BigDecimal> dailyRevenue = new HashMap<>();
        for (ParkingRecord record : records) {
            if (record.getFee() != null) {
                LocalDate date = record.getStartTime().toLocalDate();
                dailyRevenue.merge(date, record.getFee(), BigDecimal::add);
            }
        }

        return new RevenueStatistics(totalRevenue, dailyRevenue);
    }

    /**
     * 获取用户类型统计
     */
    public UserTypeStatistics getUserTypeStatistics() {
        Map<User.UserType, Long> userCountByType = new HashMap<>();
        for (User.UserType userType : User.UserType.values()) {
            long count = userRepository.countByUserType(userType);
            userCountByType.put(userType, count);
        }

        Map<User.UserType, Long> vehicleCountByUserType = new HashMap<>();
        for (User.UserType userType : User.UserType.values()) {
            long count = vehicleRepository.countByUserUserTypeAndIsTemporaryFalse(userType);
            vehicleCountByUserType.put(userType, count);
        }

        return new UserTypeStatistics(userCountByType, vehicleCountByUserType);
    }

    /**
     * 获取车辆类型统计
     */
    public VehicleTypeStatistics getVehicleTypeStatistics() {
        Map<Vehicle.VehicleType, Long> vehicleCountByType = new HashMap<>();
        for (Vehicle.VehicleType vehicleType : Vehicle.VehicleType.values()) {
            long count = vehicleRepository.countByVehicleType(vehicleType);
            vehicleCountByType.put(vehicleType, count);
        }

        return new VehicleTypeStatistics(vehicleCountByType);
    }

    /**
     * 系统统计信息类
     */
    public static class SystemStatistics {
        private final long totalUsers;
        private final long pendingUsers;
        private final long approvedUsers;
        private final long totalVehicles;
        private final long pendingVehicles;
        private final long approvedVehicles;
        private final long totalRecords;
        private final long parkingCount;
        private final long completedCount;
        private final BigDecimal totalRevenue;
        private final BigDecimal todayRevenue;

        public SystemStatistics(long totalUsers, long pendingUsers, long approvedUsers,
                                long totalVehicles, long pendingVehicles, long approvedVehicles,
                                long totalRecords, long parkingCount, long completedCount,
                                BigDecimal totalRevenue, BigDecimal todayRevenue) {
            this.totalUsers = totalUsers;
            this.pendingUsers = pendingUsers;
            this.approvedUsers = approvedUsers;
            this.totalVehicles = totalVehicles;
            this.pendingVehicles = pendingVehicles;
            this.approvedVehicles = approvedVehicles;
            this.totalRecords = totalRecords;
            this.parkingCount = parkingCount;
            this.completedCount = completedCount;
            this.totalRevenue = totalRevenue;
            this.todayRevenue = todayRevenue;
        }

        // Getters
        public long getTotalUsers() { return totalUsers; }
        public long getPendingUsers() { return pendingUsers; }
        public long getApprovedUsers() { return approvedUsers; }
        public long getTotalVehicles() { return totalVehicles; }
        public long getPendingVehicles() { return pendingVehicles; }
        public long getApprovedVehicles() { return approvedVehicles; }
        public long getTotalRecords() { return totalRecords; }
        public long getParkingCount() { return parkingCount; }
        public long getCompletedCount() { return completedCount; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public BigDecimal getTodayRevenue() { return todayRevenue; }
    }

    /**
     * 今日统计信息类
     */
    public static class TodayStatistics {
        private final long todayNewUsers;
        private final long todayNewVehicles;
        private final long todayParkingCount;
        private final BigDecimal todayRevenue;

        public TodayStatistics(long todayNewUsers, long todayNewVehicles,
                               long todayParkingCount, BigDecimal todayRevenue) {
            this.todayNewUsers = todayNewUsers;
            this.todayNewVehicles = todayNewVehicles;
            this.todayParkingCount = todayParkingCount;
            this.todayRevenue = todayRevenue;
        }

        // Getters
        public long getTodayNewUsers() { return todayNewUsers; }
        public long getTodayNewVehicles() { return todayNewVehicles; }
        public long getTodayParkingCount() { return todayParkingCount; }
        public BigDecimal getTodayRevenue() { return todayRevenue; }
    }

    /**
     * 收入统计信息类
     */
    public static class RevenueStatistics {
        private final BigDecimal totalRevenue;
        private final Map<LocalDate, BigDecimal> dailyRevenue;

        public RevenueStatistics(BigDecimal totalRevenue, Map<LocalDate, BigDecimal> dailyRevenue) {
            this.totalRevenue = totalRevenue;
            this.dailyRevenue = dailyRevenue;
        }

        // Getters
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public Map<LocalDate, BigDecimal> getDailyRevenue() { return dailyRevenue; }
    }

    /**
     * 用户类型统计信息类
     */
    public static class UserTypeStatistics {
        private final Map<User.UserType, Long> userCountByType;
        private final Map<User.UserType, Long> vehicleCountByUserType;

        public UserTypeStatistics(Map<User.UserType, Long> userCountByType,
                                  Map<User.UserType, Long> vehicleCountByUserType) {
            this.userCountByType = userCountByType;
            this.vehicleCountByUserType = vehicleCountByUserType;
        }

        // Getters
        public Map<User.UserType, Long> getUserCountByType() { return userCountByType; }
        public Map<User.UserType, Long> getVehicleCountByUserType() { return vehicleCountByUserType; }
    }

    /**
     * 车辆类型统计信息类
     */
    public static class VehicleTypeStatistics {
        private final Map<Vehicle.VehicleType, Long> vehicleCountByType;

        public VehicleTypeStatistics(Map<Vehicle.VehicleType, Long> vehicleCountByType) {
            this.vehicleCountByType = vehicleCountByType;
        }

        // Getters
        public Map<Vehicle.VehicleType, Long> getVehicleCountByType() { return vehicleCountByType; }
    }
}
