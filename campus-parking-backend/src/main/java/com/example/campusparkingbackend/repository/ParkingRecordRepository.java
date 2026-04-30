// src/main/java/com/example/campusparkingbackend/repository/ParkingRecordRepository.java
package com.example.campusparkingbackend.repository;

import com.example.campusparkingbackend.entity.ParkingRecord;
import com.example.campusparkingbackend.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingRecordRepository extends JpaRepository<ParkingRecord, Long> {

    // ========== 用户相关查询 ==========
    List<ParkingRecord> findByUserId(Long userId);
    List<ParkingRecord> findByUserIdAndStatus(Long userId, ParkingRecord.ParkingStatus status);
    Page<ParkingRecord> findByUserId(Long userId, Pageable pageable);

    // ========== 车辆相关查询 ==========
    List<ParkingRecord> findByVehicleId(Long vehicleId);

    // ✅ **修改这里：注释掉或删除重复的方法声明，保留一个**
    // 方案A：保留方法名自动生成（第30行），删除后面的@Query版本
    // 方案B：保留@Query版本，删除第30行的方法声明
    List<ParkingRecord> findByPlateNumberAndStatus(String plateNumber, ParkingRecord.ParkingStatus status);
    // 修改后：注释掉第147-151行的重复定义

    // ✅ **同样修改这里：删除重复的findByVehicleIdAndStatus**
    // List<ParkingRecord> findByVehicleIdAndStatus(Long vehicleId, ParkingRecord.ParkingStatus status);
    // 修改后：注释掉第30行的重复声明

    // ========== 停车位相关查询 ==========
    List<ParkingRecord> findByParkingSpotId(Long spotId);

    // ========== 状态查询 ==========
    List<ParkingRecord> findByStatus(ParkingRecord.ParkingStatus status);
    long countByStatus(ParkingRecord.ParkingStatus status);

    // ========== 时间范围查询 ==========
    List<ParkingRecord> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    List<ParkingRecord> findByEndTimeBetween(LocalDateTime start, LocalDateTime end);

    // ========== 统计查询 ==========
    @Query("SELECT SUM(pr.fee) FROM ParkingRecord pr WHERE pr.status = 'COMPLETED' AND pr.fee IS NOT NULL")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT pr FROM ParkingRecord pr WHERE pr.user.id = :userId AND pr.status = 'PARKING'")
    List<ParkingRecord> findActiveParkingByUserId(@Param("userId") Long userId);

    @Query("SELECT pr FROM ParkingRecord pr WHERE pr.parkingSpot.id = :spotId AND pr.status = 'PARKING'")
    Optional<ParkingRecord> findActiveParkingBySpotId(@Param("spotId") Long spotId);

    @Query("SELECT SUM(pr.fee) FROM ParkingRecord pr WHERE pr.status = 'COMPLETED' AND DATE(pr.endTime) = CURRENT_DATE")
    BigDecimal calculateTodayRevenue();

    @Query("SELECT pr FROM ParkingRecord pr WHERE pr.vehicle.plateNumber = :plateNumber")
    List<ParkingRecord> findByVehiclePlateNumber(@Param("plateNumber") String plateNumber);

    @Query("SELECT COUNT(pr) FROM ParkingRecord pr WHERE DATE(pr.startTime) = CURRENT_DATE")
    long countTodayParkingRecords();

    @Query("SELECT SUM(pr.fee) FROM ParkingRecord pr WHERE pr.user.id = :userId AND pr.status = 'COMPLETED' AND pr.fee IS NOT NULL")
    BigDecimal calculateUserTotalSpending(@Param("userId") Long userId);

    @Query("SELECT pr FROM ParkingRecord pr WHERE pr.user.id = :userId ORDER BY pr.startTime DESC")
    List<ParkingRecord> findUserRecentRecords(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT SUM(pr.fee) FROM ParkingRecord pr WHERE " +
            "pr.userType = 'EXTERNAL_USER' AND " +
            "pr.status = 'COMPLETED' AND " +
            "pr.fee IS NOT NULL AND " +
            "(:startTime IS NULL OR pr.endTime >= :startTime) AND " +
            "(:endTime IS NULL OR pr.endTime <= :endTime)")
    BigDecimal calculateExternalUserRevenueByDateRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(pr) FROM ParkingRecord pr WHERE " +
            "pr.userType = 'EXTERNAL_USER' AND " +
            "(:startTime IS NULL OR pr.startTime >= :startTime) AND " +
            "(:endTime IS NULL OR pr.startTime <= :endTime)")
    long countExternalUserRecordsByDateRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(pr) FROM ParkingRecord pr WHERE " +
            "pr.userType = 'EXTERNAL_USER' AND " +
            "pr.status = 'PARKING'")
    long countCurrentExternalUserParking();

    @Query("SELECT pr FROM ParkingRecord pr WHERE " +
            "pr.userType = 'EXTERNAL_USER' AND " +
            "(:plateNumber IS NULL OR pr.plateNumber LIKE %:plateNumber%) AND " +
            "(:startTime IS NULL OR pr.startTime >= :startTime) AND " +
            "(:endTime IS NULL OR pr.startTime <= :endTime)")
    Page<ParkingRecord> findExternalUserRecordsWithFilters(
            @Param("plateNumber") String plateNumber,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    // ❌ **删除这个重复的定义（第147-151行）**
    // @Query("SELECT pr FROM ParkingRecord pr WHERE pr.plateNumber = :plateNumber AND pr.status = :status")
    // List<ParkingRecord> findByPlateNumberAndStatus(
    //         @Param("plateNumber") String plateNumber,
    //         @Param("status") ParkingRecord.ParkingStatus status
    // );

    // ❌ **删除这个重复的定义（第153-157行）**
    // @Query("SELECT pr FROM ParkingRecord pr WHERE pr.vehicle.id = :vehicleId AND pr.status = :status")
    // List<ParkingRecord> findByVehicleIdAndStatus(
    //         @Param("vehicleId") Long vehicleId,
    //         @Param("status") ParkingRecord.ParkingStatus status
    // );

    @Query("SELECT DATE(pr.startTime), COUNT(pr) FROM ParkingRecord pr WHERE " +
            "pr.userType = 'EXTERNAL_USER' AND " +
            "YEAR(pr.startTime) = :year AND MONTH(pr.startTime) = :month " +
            "GROUP BY DATE(pr.startTime) ORDER BY COUNT(pr) DESC")
    List<Object[]> findBusiestDateInMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT DATE(pr.endTime), SUM(pr.fee) FROM ParkingRecord pr WHERE " +
            "pr.userType = 'EXTERNAL_USER' AND " +
            "pr.status = 'COMPLETED' AND " +
            "pr.fee IS NOT NULL AND " +
            "pr.endTime BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(pr.endTime) ORDER BY DATE(pr.endTime)")
    List<Object[]> findDailyExternalRevenue(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ========== 通用查询 ==========
    List<ParkingRecord> findByParkingLotName(String parkingLotName);
    List<ParkingRecord> findByUserType(String userType);

    @Query("SELECT pr FROM ParkingRecord pr WHERE pr.userType IN :userTypes")
    List<ParkingRecord> findByUserTypeIn(@Param("userTypes") List<String> userTypes);

    List<ParkingRecord> findByParkingLotNameAndStatus(String parkingLotName, ParkingRecord.ParkingStatus status);
    List<ParkingRecord> findByUserOrderByStartTimeDesc(User user);
    List<ParkingRecord> findByUserAndStatus(User user, ParkingRecord.ParkingStatus status);
    List<ParkingRecord> findByPaymentStatus(ParkingRecord.PaymentStatus paymentStatus);
    List<ParkingRecord> findByParkingLotNameAndStartTimeBetween(String parkingLotName, LocalDateTime start, LocalDateTime end);
    List<ParkingRecord> findByUserTypeAndStatus(String userType, ParkingRecord.ParkingStatus status);
    List<ParkingRecord> findByParkingLotNameAndUserType(String parkingLotName, String userType);
    // 添加或修正这个方法
    List<ParkingRecord> findByVehicleIdAndStatus(Long vehicleId, ParkingRecord.ParkingStatus status);
    // ========== 停车场统计 ==========
    @Query("SELECT DISTINCT pr.parkingLotName FROM ParkingRecord pr")
    List<String> findAllParkingLotNames();

    @Query("SELECT pr.parkingLotName, COUNT(pr) FROM ParkingRecord pr GROUP BY pr.parkingLotName")
    List<Object[]> countParkingRecordsByParkingLot();

    @Query("SELECT pr.parkingLotName, SUM(pr.fee) FROM ParkingRecord pr WHERE pr.status = 'COMPLETED' AND pr.fee IS NOT NULL GROUP BY pr.parkingLotName")
    List<Object[]> calculateRevenueByParkingLot();

    // ========== 搜索查询 ==========
    @Query("SELECT pr FROM ParkingRecord pr WHERE pr.plateNumber LIKE %:plateNumber%")
    List<ParkingRecord> findByPlateNumberContaining(@Param("plateNumber") String plateNumber);

    @Query("SELECT pr FROM ParkingRecord pr WHERE pr.userName LIKE %:userName%")
    List<ParkingRecord> findByUserNameContaining(@Param("userName") String userName);

    @Query("SELECT pr FROM ParkingRecord pr WHERE " +
            "(:parkingLotName IS NULL OR pr.parkingLotName = :parkingLotName) AND " +
            "(:plateNumber IS NULL OR pr.plateNumber LIKE %:plateNumber%) AND " +
            "(:startTime IS NULL OR pr.startTime >= :startTime) AND " +
            "(:endTime IS NULL OR pr.startTime <= :endTime)")
    List<ParkingRecord> searchParkingRecords(
            @Param("parkingLotName") String parkingLotName,
            @Param("plateNumber") String plateNumber,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // ========== 用户类型分组查询 ==========
    @Query("SELECT pr FROM ParkingRecord pr WHERE pr.userType IN ('STUDENT', 'TEACHER', 'STAFF')")
    List<ParkingRecord> findCampusUserRecords();

    @Query("SELECT pr FROM ParkingRecord pr WHERE pr.userType = 'EXTERNAL_USER'")
    List<ParkingRecord> findExternalUserRecords();

    // ========== 今日统计 ==========
    @Query("SELECT pr.parkingLotName, COUNT(pr) FROM ParkingRecord pr WHERE DATE(pr.startTime) = CURRENT_DATE GROUP BY pr.parkingLotName")
    List<Object[]> countTodayParkingByParkingLot();

    @Query("SELECT pr.parkingLotName, SUM(pr.fee) FROM ParkingRecord pr WHERE pr.status = 'COMPLETED' AND DATE(pr.endTime) = CURRENT_DATE AND pr.fee IS NOT NULL GROUP BY pr.parkingLotName")
    List<Object[]> calculateTodayRevenueByParkingLot();

    // ========== 用户停车场关联查询 ==========
    List<ParkingRecord> findByUserIdAndParkingLotName(Long userId, String parkingLotName);

    @Query("SELECT pr FROM ParkingRecord pr WHERE pr.user.id = :userId AND pr.parkingLotName = :parkingLotName AND pr.status = 'PARKING'")
    Optional<ParkingRecord> findActiveParkingByUserAndParkingLot(@Param("userId") Long userId, @Param("parkingLotName") String parkingLotName);

    @Query("SELECT pr.parkingLotName, COUNT(pr) FROM ParkingRecord pr WHERE pr.user.id = :userId GROUP BY pr.parkingLotName")
    List<Object[]> countUserParkingByParkingLot(@Param("userId") Long userId);
    /**
     * 统计车辆关联的停车记录数量
     */
    @Query("SELECT COUNT(pr) FROM ParkingRecord pr WHERE pr.vehicle.id = :vehicleId")
    long countByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query("SELECT pr FROM ParkingRecord pr WHERE pr.parkingLotName = :parkingLotName AND pr.status = 'PARKING'")
    List<ParkingRecord> findActiveParkingByParkingLot(@Param("parkingLotName") String parkingLotName);

    @Query("SELECT COUNT(pr) FROM ParkingRecord pr WHERE pr.parkingLotName = :parkingLotName AND pr.status = 'PARKING'")
    long countActiveParkingByParkingLot(@Param("parkingLotName") String parkingLotName);

    List<ParkingRecord> findByUserIdAndParkingLotNameAndStatus(Long userId, String parkingLotName, ParkingRecord.ParkingStatus status);

    // ========== 分页和规格查询 ==========
    Page<ParkingRecord> findAll(Specification<ParkingRecord> spec, Pageable pageable);

    // ========== 月度统计 ==========
    @Query("SELECT COALESCE(SUM(pr.fee), 0) FROM ParkingRecord pr WHERE " +
            "pr.userType = 'EXTERNAL_USER' AND " +
            "pr.status = 'COMPLETED' AND " +
            "pr.fee IS NOT NULL AND " +
            "YEAR(pr.endTime) = :year AND MONTH(pr.endTime) = :month")
    BigDecimal calculateMonthlyRevenue(@Param("year") int year, @Param("month") int month);

    // ========== 实时数据方法 ==========
    // 统一使用 endTime 进行统计，确保与收入趋势一致
    @Query("SELECT COUNT(pr) FROM ParkingRecord pr WHERE " +
            "pr.userType = 'EXTERNAL_USER' AND " +
            "pr.status = 'COMPLETED' AND " +
            "pr.fee IS NOT NULL AND " +
            "(:startTime IS NULL OR pr.endTime >= :startTime) AND " +
            "(:endTime IS NULL OR pr.endTime <= :endTime)")
    long countExternalUserCompletedRecordsByDateRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT pr FROM ParkingRecord pr WHERE " +
            "pr.userType = 'EXTERNAL_USER' AND " +
            "pr.status = 'COMPLETED' AND " +
            "pr.fee IS NOT NULL AND " +
            "(:startTime IS NULL OR pr.endTime >= :startTime) AND " +
            "(:endTime IS NULL OR pr.endTime <= :endTime) AND " +
            "(:plateNumber IS NULL OR pr.plateNumber LIKE %:plateNumber%)")
    Page<ParkingRecord> findExternalUserCompletedRecordsWithFilters(
            @Param("plateNumber") String plateNumber,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
    @Modifying
    @Query("DELETE FROM ParkingRecord pr WHERE pr.startTime < :cutoffTime")
    long deleteByStartTimeBefore(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT pr FROM ParkingRecord pr WHERE pr.status = 'PARKING' ORDER BY pr.startTime DESC")
    List<ParkingRecord> findRealTimeActiveParking();

    @Query("SELECT COUNT(pr) FROM ParkingRecord pr WHERE DATE(pr.startTime) = CURRENT_DATE")
    long countRealTimeTodayRecords();

    @Query("SELECT pr.parkingLotName, COUNT(pr) FROM ParkingRecord pr " +
            "WHERE pr.status = 'PARKING' AND DATE(pr.startTime) = CURRENT_DATE " +
            "GROUP BY pr.parkingLotName")
    List<Object[]> countRealTimeParkingByLot();
    // 添加删除方法
    @Modifying
    @Transactional
    @Query("DELETE FROM ParkingRecord pr WHERE pr.vehicle.id = :vehicleId")
    int deleteByVehicleId(@Param("vehicleId") Long vehicleId);
    @Query("SELECT COALESCE(SUM(pr.fee), 0) FROM ParkingRecord pr " +
            "WHERE pr.status = 'COMPLETED' AND DATE(pr.endTime) = CURRENT_DATE")
    BigDecimal calculateRealTimeTodayRevenue();

    @Query("SELECT pr FROM ParkingRecord pr WHERE pr.startTime >= :todayStart ORDER BY pr.startTime DESC")
    List<ParkingRecord> findRealTimeRecords(@Param("todayStart") LocalDateTime todayStart);
}
