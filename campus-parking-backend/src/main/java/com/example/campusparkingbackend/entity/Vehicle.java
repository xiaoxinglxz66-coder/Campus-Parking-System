package com.example.campusparkingbackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plate_number", unique = true, nullable = false)
    private String plateNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type")
    private VehicleType vehicleType = VehicleType.CAR;

    @Column(name = "brand")
    private String brand;

    @Column(name = "color")
    private String color;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"vehicles", "password"}) // ✅ 防止循环引用，忽略敏感字段
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private VehicleStatus status = VehicleStatus.PENDING;

    @Column(name = "is_temporary")
    private Boolean isTemporary = false;

    @Column(name = "created_time")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 🆕 添加与 ParkingRecord 的关联关系
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore // 避免序列化循环
    private List<ParkingRecord> parkingRecords = new ArrayList<>();

    // 构造器
    public Vehicle() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isTemporary = false; // 默认不是临时车辆
        this.status = VehicleStatus.PENDING; // 默认待审核
    }

    public Vehicle(User user, String plateNumber, VehicleType vehicleType, String brand, String color) {
        this();
        this.user = user;
        this.plateNumber = plateNumber;
        this.vehicleType = vehicleType;
        this.brand = brand;
        this.color = color;
        this.status = VehicleStatus.PENDING;
        this.isTemporary = false;
    }

    // 临时车辆构造器
    public Vehicle(String plateNumber) {
        this();
        this.plateNumber = plateNumber;
        this.vehicleType = VehicleType.CAR;
        this.brand = "临时车辆";
        this.color = "未知";
        this.status = VehicleStatus.APPROVED;
        this.isTemporary = true;
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus status) { this.status = status; }

    public Boolean getIsTemporary() { return isTemporary; }
    public void setIsTemporary(Boolean isTemporary) { this.isTemporary = isTemporary; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // 🆕 ParkingRecords 的 getter 和 setter
    public List<ParkingRecord> getParkingRecords() { return parkingRecords; }
    public void setParkingRecords(List<ParkingRecord> parkingRecords) {
        this.parkingRecords = parkingRecords;
    }

    // 枚举定义
    public enum VehicleType {
        CAR, MOTORCYCLE, ELECTRIC_CAR
    }

    public enum VehicleStatus {
        PENDING,    // 待审核
        APPROVED,   // 已审核
        REJECTED    // 已拒绝
    }

    // 🆕 辅助方法：添加停车记录
    public void addParkingRecord(ParkingRecord record) {
        parkingRecords.add(record);
        record.setVehicle(this);
    }

    // 🆕 辅助方法：移除停车记录
    public void removeParkingRecord(ParkingRecord record) {
        parkingRecords.remove(record);
        record.setVehicle(null);
    }

    // 🆕 清空所有停车记录
    public void clearParkingRecords() {
        for (ParkingRecord record : new ArrayList<>(parkingRecords)) {
            removeParkingRecord(record);
        }
    }
}
