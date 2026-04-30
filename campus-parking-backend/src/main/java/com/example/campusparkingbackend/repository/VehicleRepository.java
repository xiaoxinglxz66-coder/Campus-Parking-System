// src/main/java/com/example/campusparkingbackend/repository/VehicleRepository.java
package com.example.campusparkingbackend.repository;

import com.example.campusparkingbackend.entity.ParkingRecord;
import com.example.campusparkingbackend.entity.User;
import com.example.campusparkingbackend.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // 根据车牌号查找（所有类型车辆）
    Optional<Vehicle> findByPlateNumber(String plateNumber);

    // 根据车牌号查找临时车辆
    List<Vehicle> findByPlateNumberAndIsTemporaryTrue(String plateNumber);

    // 检查车牌号是否存在（排除临时车辆）
    @Query("SELECT COUNT(v) > 0 FROM Vehicle v WHERE v.plateNumber = :plateNumber AND v.isTemporary = false")
    boolean existsByPlateNumber(@Param("plateNumber") String plateNumber);

    // 检查车牌号是否存在（包括所有车辆）
    @Query("SELECT COUNT(v) > 0 FROM Vehicle v WHERE v.plateNumber = :plateNumber")
    boolean existsByPlateNumberIncludeTemporary(@Param("plateNumber") String plateNumber);

    // 根据用户ID查找车辆
    List<Vehicle> findByUserId(Long userId);

    // 根据车辆类型查找
    List<Vehicle> findByVehicleType(Vehicle.VehicleType vehicleType);

    // 根据状态查找车辆
    List<Vehicle> findByStatus(Vehicle.VehicleStatus status);

    // 根据用户类型查找车辆
    List<Vehicle> findByUserUserType(User.UserType userType);
    Optional<Vehicle> findByPlateNumberAndUserIdAndStatus(
            String plateNumber,
            Long userId,
            Vehicle.VehicleStatus status
    );
    List<ParkingRecord> findByPlateNumberAndStatus(String plateNumber, ParkingRecord.ParkingStatus status);
    // 根据用户类型列表查找非临时车辆
    @Query("SELECT v FROM Vehicle v WHERE v.user.userType IN :userTypes AND v.isTemporary = false")
    List<Vehicle> findByUserUserTypeInAndIsTemporaryFalse(@Param("userTypes") List<User.UserType> userTypes);

    // 根据用户类型查找非临时车辆
    @Query("SELECT v FROM Vehicle v WHERE v.user.userType = :userType AND v.isTemporary = false")
    List<Vehicle> findByUserUserTypeAndIsTemporaryFalse(@Param("userType") User.UserType userType);

    // 查找临时车辆
    List<Vehicle> findByIsTemporaryTrue();

    // 查找非临时车辆
    List<Vehicle> findByIsTemporaryFalse();

    // 根据状态和用户类型查找车辆
    @Query("SELECT v FROM Vehicle v WHERE v.status = :status AND v.user.userType = :userType")
    List<Vehicle> findByStatusAndUserUserType(@Param("status") Vehicle.VehicleStatus status,
                                              @Param("userType") User.UserType userType);

    // 统计各类车辆数量
    long countByStatus(Vehicle.VehicleStatus status);
    long countByIsTemporaryTrue();
    long countByIsTemporaryFalse();

    // 根据用户ID和状态查找车辆
    List<Vehicle> findByUserIdAndStatus(Long userId, Vehicle.VehicleStatus status);

    // 查找正在停车中的车辆
    @Query("SELECT v FROM Vehicle v WHERE v.id IN (" +
            "SELECT pr.vehicle.id FROM ParkingRecord pr WHERE pr.status = 'PARKING'" +
            ") AND v.id = :vehicleId")
    Optional<Vehicle> findVehicleInParking(@Param("vehicleId") Long vehicleId);

    // 查找用户正在停车中的车辆
    @Query("SELECT v FROM Vehicle v WHERE v.user.id = :userId AND v.id IN (" +
            "SELECT pr.vehicle.id FROM ParkingRecord pr WHERE pr.status = 'PARKING'" +
            ")")
    List<Vehicle> findUserVehiclesInParking(@Param("userId") Long userId);

    // 根据车牌号模糊搜索
    @Query("SELECT v FROM Vehicle v WHERE v.plateNumber LIKE %:plateNumber%")
    List<Vehicle> findByPlateNumberContaining(@Param("plateNumber") String plateNumber);

    // 根据品牌模糊搜索
    @Query("SELECT v FROM Vehicle v WHERE v.brand LIKE %:brand%")
    List<Vehicle> findByBrandContaining(@Param("brand") String brand);

    // 根据用户真实姓名查找车辆
    @Query("SELECT v FROM Vehicle v WHERE v.user.realName LIKE %:realName%")
    List<Vehicle> findByUserRealNameContaining(@Param("realName") String realName);

    // 获取待审核的校内用户车辆
    @Query("SELECT v FROM Vehicle v WHERE v.status = 'PENDING' AND v.isTemporary = false AND v.user.userType IN :userTypes")
    List<Vehicle> findPendingCampusUserVehicles(@Param("userTypes") List<User.UserType> userTypes);

    // 获取已审核的校内用户车辆
    @Query("SELECT v FROM Vehicle v WHERE v.status = 'APPROVED' AND v.isTemporary = false AND v.user.userType IN :userTypes")
    List<Vehicle> findApprovedCampusUserVehicles(@Param("userTypes") List<User.UserType> userTypes);

    // 根据多个ID查找车辆
    List<Vehicle> findByIdIn(List<Long> ids);

    // 统计用户车辆数量
    long countByUserId(Long userId);

    // 统计用户某种状态的车辆数量
    long countByUserIdAndStatus(Long userId, Vehicle.VehicleStatus status);

    // === 数据隔离方法 ===

    // 根据用户ID和用户类型查找车辆（核心数据隔离方法）
    @Query("SELECT v FROM Vehicle v WHERE v.user.id = :userId AND v.user.userType = :userType")
    List<Vehicle> findByUserIdAndUserUserType(@Param("userId") Long userId,
                                              @Param("userType") User.UserType userType);

    // 根据用户类型统计车辆数量
    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.user.userType = :userType AND v.isTemporary = false")
    long countByUserUserTypeAndIsTemporaryFalse(@Param("userType") User.UserType userType);
    @Query("SELECT v FROM Vehicle v LEFT JOIN FETCH v.user")
    List<Vehicle> findAllWithUser();
    // 根据用户ID和用户类型统计车辆数量
    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.user.id = :userId AND v.user.userType = :userType")
    long countByUserIdAndUserUserType(@Param("userId") Long userId,
                                      @Param("userType") User.UserType userType);

    // 查找校内用户待审核车辆（按用户类型隔离）
    @Query("SELECT v FROM Vehicle v WHERE v.status = 'PENDING' AND v.isTemporary = false AND v.user.userType = :userType")
    List<Vehicle> findPendingVehiclesByUserType(@Param("userType") User.UserType userType);

    // 查找校内用户已审核车辆（按用户类型隔离）
    @Query("SELECT v FROM Vehicle v WHERE v.status = 'APPROVED' AND v.isTemporary = false AND v.user.userType = :userType")
    List<Vehicle> findApprovedVehiclesByUserType(@Param("userType") User.UserType userType);

    // 根据用户类型和车牌号查找车辆
    @Query("SELECT v FROM Vehicle v WHERE v.user.userType = :userType AND v.plateNumber = :plateNumber")
    Optional<Vehicle> findByUserUserTypeAndPlateNumber(@Param("userType") User.UserType userType,
                                                       @Param("plateNumber") String plateNumber);

    // 统计用户类型的临时车辆数量
    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.user.userType = :userType AND v.isTemporary = true")
    long countTemporaryVehiclesByUserType(@Param("userType") User.UserType userType);

    // 查找用户类型的所有车辆（包括临时车辆）
    @Query("SELECT v FROM Vehicle v WHERE v.user.userType = :userType")
    List<Vehicle> findAllByUserUserType(@Param("userType") User.UserType userType);

    // 根据用户类型和状态查找车辆
    @Query("SELECT v FROM Vehicle v WHERE v.user.userType = :userType AND v.status = :status")
    List<Vehicle> findByUserUserTypeAndStatus(@Param("userType") User.UserType userType,
                                              @Param("status") Vehicle.VehicleStatus status);

    // 时间范围查询
    List<Vehicle> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // 按车辆类型统计
    long countByVehicleType(Vehicle.VehicleType vehicleType);

    // 查找用户自己的临时车辆
    @Query("SELECT v FROM Vehicle v WHERE v.user.id = :userId AND v.isTemporary = true")
    List<Vehicle> findUserTemporaryVehicles(@Param("userId") Long userId);

    // 查找用户自己的非临时车辆
    @Query("SELECT v FROM Vehicle v WHERE v.user.id = :userId AND v.isTemporary = false")
    List<Vehicle> findUserPermanentVehicles(@Param("userId") Long userId);

    // 统计用户自己的车辆数量（按类型）
    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.user.id = :userId AND v.isTemporary = :isTemporary")
    long countByUserIdAndIsTemporary(@Param("userId") Long userId,
                                     @Param("isTemporary") Boolean isTemporary);
}
