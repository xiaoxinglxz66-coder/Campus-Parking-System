// src/main/java/com/example/campusparkingbackend/repository/UserRepository.java
package com.example.campusparkingbackend.repository;

import com.example.campusparkingbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 根据用户名查找用户
    Optional<User> findByUsername(String username);

    // 根据手机号查找用户
    Optional<User> findByPhone(String phone);

    // 根据邮箱查找用户
    Optional<User> findByEmail(String email);

    // 根据用户类型查找用户
    List<User> findByUserType(User.UserType userType);

    // 根据用户类型和状态查询
    List<User> findByUserTypeAndUserStatus(User.UserType userType, User.UserStatus userStatus);

    // 根据用户状态查找用户
    List<User> findByUserStatus(User.UserStatus userStatus);

    // 根据用户类型和状态查找用户
    @Query("SELECT u FROM User u WHERE u.userType IN :userTypes AND u.userStatus = :userStatus")
    List<User> findByUserTypeInAndUserStatus(@Param("userTypes") List<User.UserType> userTypes,
                                             @Param("userStatus") User.UserStatus userStatus);

    // 检查用户名是否存在
    boolean existsByUsername(String username);

    // 检查手机号是否存在
    boolean existsByPhone(String phone);

    // 检查邮箱是否存在
    boolean existsByEmail(String email);

    // 根据真实姓名模糊搜索
    @Query("SELECT u FROM User u WHERE u.realName LIKE %:realName%")
    List<User> findByRealNameContaining(@Param("realName") String realName);

    // 根据用户名模糊搜索
    @Query("SELECT u FROM User u WHERE u.username LIKE %:username%")
    List<User> findByUsernameContaining(@Param("username") String username);

    // 统计各类用户数量
    long countByUserType(User.UserType userType);
    long countByUserStatus(User.UserStatus userStatus);

    // 根据用户类型统计待审核用户
    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = :userType AND u.userStatus = 'PENDING'")
    long countPendingUsersByType(@Param("userType") User.UserType userType);

    // 获取所有待审核用户
    @Query("SELECT u FROM User u WHERE u.userStatus = 'PENDING'")
    List<User> findPendingUsers();

    // 获取所有已审核用户
    @Query("SELECT u FROM User u WHERE u.userStatus = 'APPROVED'")
    List<User> findApprovedUsers();

    // 获取所有已拒绝用户
    @Query("SELECT u FROM User u WHERE u.userStatus = 'REJECTED'")
    List<User> findRejectedUsers();

    // 根据用户类型获取待审核用户
    @Query("SELECT u FROM User u WHERE u.userType = :userType AND u.userStatus = 'PENDING'")
    List<User> findPendingUsersByType(@Param("userType") User.UserType userType);

    // 检查用户是否有停车记录
    @Query("SELECT COUNT(pr) > 0 FROM ParkingRecord pr WHERE pr.user.id = :userId")
    boolean hasParkingRecords(@Param("userId") Long userId);

    // 检查用户是否有车辆
    @Query("SELECT COUNT(v) > 0 FROM Vehicle v WHERE v.user.id = :userId")
    boolean hasVehicles(@Param("userId") Long userId);

    // 根据余额范围查找用户
    @Query("SELECT u FROM User u WHERE u.balance BETWEEN :minBalance AND :maxBalance")
    List<User> findByBalanceBetween(@Param("minBalance") Double minBalance,
                                    @Param("maxBalance") Double maxBalance);
    // 在 UserRepository 中添加
    List<User> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // 获取需要支付的用户（校外用户且余额不足）
    @Query("SELECT u FROM User u WHERE u.userType = 'EXTERNAL_USER' AND u.needPayment = true")
    List<User> findExternalUsersNeedPayment();

    // 统计用户总余额
    @Query("SELECT SUM(u.balance) FROM User u WHERE u.balance IS NOT NULL")
    Double getTotalUserBalance();
}
