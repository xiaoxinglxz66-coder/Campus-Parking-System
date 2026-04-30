// src/main/java/com/example/campusparkingbackend/entity/User.java
package com.example.campusparkingbackend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "real_name", length = 50)
    private String realName;

    @Column(name = "phone", unique = true, length = 20)
    private String phone;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false, length = 20)
    private UserStatus userStatus = UserStatus.PENDING;

    @Column(name = "balance")
    private Double balance = 0.0;

    @Column(name = "need_payment")
    private Boolean needPayment = false;

    @Column(name = "created_time")
    private LocalDateTime createdAt;

    @Column(name = "updated_time")
    private LocalDateTime updatedAt; // 统一使用 updatedAt

    @Column(name = "student_id", unique = true, length = 20)
    private String studentId; // 新增学号字段

    // 枚举定义
    public enum UserType {
        STUDENT, TEACHER, STAFF, ADMIN, EXTERNAL_USER
    }

    public enum UserStatus {
        PENDING, APPROVED, REJECTED, DELETED
    }

    // 构造器
    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    public UserStatus getUserStatus() { return userStatus; }
    public void setUserStatus(UserStatus userStatus) { this.userStatus = userStatus; }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }

    public Boolean getNeedPayment() { return needPayment; }
    public void setNeedPayment(Boolean needPayment) { this.needPayment = needPayment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
}
