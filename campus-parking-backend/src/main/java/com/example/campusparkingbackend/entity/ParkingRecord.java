// src/main/java/com/example/campusparkingbackend/entity/ParkingRecord.java
package com.example.campusparkingbackend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_records")
public class ParkingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_spot_id")
    private ParkingSpot parkingSpot;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "fee", precision = 10, scale = 2)
    private BigDecimal fee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ParkingStatus status;

    // 新增字段 - 停车场名称
    @Column(name = "parking_lot_name")
    private String parkingLotName;

    // 新增字段 - 车牌号（冗余存储，方便查询）
    @Column(name = "plate_number")
    private String plateNumber;

    // 新增字段 - 用户名（冗余存储，方便查询）
    @Column(name = "user_name")
    private String userName;

    // 新增字段 - 用户类型（使用String类型避免类型不匹配）
    @Column(name = "user_type")
    private String userType;

    // 添加支付相关字段
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus; // UNPAID, PAID, REFUNDED

    @Column(name = "out_trade_no")
    private String outTradeNo; // 商户订单号

    @Column(name = "trade_no")
    private String tradeNo; // 支付宝交易号

    @Column(name = "paid_time")
    private LocalDateTime paidTime;

    @Column(name = "created_time")
    private LocalDateTime createdAt;

    @Column(name = "updated_time")
    private LocalDateTime updatedAt;

    // 停车状态枚举
    public enum ParkingStatus {
        PARKING,      // 停车中
        COMPLETED,    // 已完成
        CANCELLED     // 已取消
    }

    // 支付状态枚举
    public enum PaymentStatus {
        UNPAID,       // 未支付
        PAID,         // 已支付
        REFUNDED,     // 已退款
        FAILED        // 支付失败
    }

    // 构造器
    public ParkingRecord() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = ParkingStatus.PARKING;
        this.paymentStatus = PaymentStatus.UNPAID;
    }

    // 新增：带参数的构造器
    public ParkingRecord(User user, Vehicle vehicle, ParkingSpot spot) {
        this();
        this.user = user;
        this.vehicle = vehicle;
        this.parkingSpot = spot;
        this.startTime = LocalDateTime.now();
        this.parkingLotName = spot.getZone();
        this.plateNumber = vehicle.getPlateNumber();

        // 根据用户类型设置用户名
        if (user.getUserType() == User.UserType.EXTERNAL_USER) {
            this.userName = user.getPhone() != null ? user.getPhone() : "校外用户";
        } else {
            this.userName = user.getRealName() != null ? user.getRealName() : user.getUsername();
        }
        this.userType = user.getUserType().name();
    }

    // 新增：临时停车构造器
    public ParkingRecord(User user, String plateNumber, ParkingSpot spot) {
        this();
        this.user = user;
        this.parkingSpot = spot;
        this.startTime = LocalDateTime.now();
        this.parkingLotName = spot.getZone();
        this.plateNumber = plateNumber;

        // 根据用户类型设置用户名
        if (user.getUserType() == User.UserType.EXTERNAL_USER) {
            this.userName = user.getPhone() != null ? user.getPhone() : "校外用户";
        } else {
            this.userName = user.getRealName() != null ? user.getRealName() : user.getUsername();
        }
        this.userType = user.getUserType().name();
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    // 在 ParkingRecord.java 中修改 setUser 方法
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            // 根据用户类型决定用户名显示
            if (user.getUserType() == User.UserType.EXTERNAL_USER) {
                // 校外用户：使用电话号码作为用户名
                this.userName = user.getPhone() != null ? user.getPhone() : "校外用户";
            } else {
                // 校内用户：使用真实姓名
                this.userName = user.getRealName() != null ? user.getRealName() : user.getUsername();
            }
            this.userType = user.getUserType().name();
        }
        this.updatedAt = LocalDateTime.now();
    }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        if (vehicle != null) {
            this.plateNumber = vehicle.getPlateNumber();
        }
        this.updatedAt = LocalDateTime.now();
    }

    public ParkingSpot getParkingSpot() { return parkingSpot; }
    public void setParkingSpot(ParkingSpot parkingSpot) {
        this.parkingSpot = parkingSpot;
        if (parkingSpot != null) {
            this.parkingLotName = parkingSpot.getZone();
        }
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) {
        this.fee = fee;
        this.updatedAt = LocalDateTime.now();
    }

    public ParkingStatus getStatus() { return status; }
    public void setStatus(ParkingStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getParkingLotName() { return parkingLotName; }
    public void setParkingLotName(String parkingLotName) {
        this.parkingLotName = parkingLotName;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
        this.updatedAt = LocalDateTime.now();
    }

    public String getUserName() { return userName; }
    public void setUserName(String userName) {
        this.userName = userName;
        this.updatedAt = LocalDateTime.now();
    }

    public String getUserType() { return userType; }
    public void setUserType(String userType) {
        this.userType = userType;
        this.updatedAt = LocalDateTime.now();
    }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
        this.updatedAt = LocalDateTime.now();
    }

    public String getTradeNo() { return tradeNo; }
    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getPaidTime() { return paidTime; }
    public void setPaidTime(LocalDateTime paidTime) {
        this.paidTime = paidTime;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // 业务方法
    public void completeParking() {
        this.status = ParkingStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void cancelParking() {
        this.status = ParkingStatus.CANCELLED;
        this.endTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsPaid(String tradeNo) {
        this.paymentStatus = PaymentStatus.PAID;
        this.tradeNo = tradeNo;
        this.paidTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 计算停车时长（分钟）
    public long calculateDurationInMinutes() {
        LocalDateTime end = this.endTime != null ? this.endTime : LocalDateTime.now();
        return java.time.Duration.between(this.startTime, end).toMinutes();
    }

    // 计算停车时长（小时，向上取整）
    public long calculateDurationInHours() {
        long minutes = calculateDurationInMinutes();
        return (minutes + 59) / 60; // 向上取整
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
