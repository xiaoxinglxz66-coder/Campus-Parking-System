// src/main/java/com/example/campusparkingbackend/entity/ParkingSpot.java
package com.example.campusparkingbackend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_spots_old")
public class ParkingSpot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spot_number", unique = true, nullable = false)
    private String spotNumber;

    @Column(name = "zone")
    private String zone;

    @Column(name = "spot_type")
    @Enumerated(EnumType.STRING)
    private SpotType spotType = SpotType.REGULAR;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private SpotStatus status = SpotStatus.AVAILABLE;

    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate = new BigDecimal("5.00");

    @Column(name = "location")
    private String location = "未指定";
    // 🔥 添加数据库中存在的其他字段
    @Column(name = "parking_lot_id")
    private Long parkingLotId;

    @Column(name = "spot_in_lot_number")
    private String spotInLotNumber;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    // 构造器
    public ParkingSpot() {}

    public ParkingSpot(String spotNumber, String zone, SpotType spotType, BigDecimal hourlyRate) {
        this.spotNumber = spotNumber;
        this.zone = zone;
        this.spotType = spotType;
        this.hourlyRate = hourlyRate;
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSpotNumber() { return spotNumber; }
    public void setSpotNumber(String spotNumber) { this.spotNumber = spotNumber; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public SpotType getSpotType() { return spotType; }
    public void setSpotType(SpotType spotType) { this.spotType = spotType; }

    public SpotStatus getStatus() { return status; }
    public void setStatus(SpotStatus status) { this.status = status; }

    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    // 🔥 添加预保存和预更新回调
    @PrePersist
    protected void onCreate() {
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }
    // 枚举定义
    public enum SpotType {
        REGULAR, DISABLED, RESERVED
    }

    public enum SpotStatus {
        AVAILABLE,      // 空闲
        OCCUPIED,       // 占用（停车中）
        MAINTENANCE     // 维护中
    }

    // 停车场区域常量 - 使用内部静态类
    public static class ParkingZones {
        public static final String DINING_HALL = "一食堂停车区";
        public static final String STUDENT_CENTER = "学生活动中心";
        public static final String IFLYTEK_BUILDING = "科大讯飞楼";
        public static final String NAN_GUI_GU_A = "南硅谷A";
        public static final String STARTUP_PARK = "创业园";
        public static final String PERFORMANCE_CENTER = "演艺中心";
        public static final String FOOD_STREET = "美食街";

        public static String[] getAllParkingZones() {
            return new String[]{
                    DINING_HALL, STUDENT_CENTER, IFLYTEK_BUILDING,
                    NAN_GUI_GU_A, STARTUP_PARK, PERFORMANCE_CENTER, FOOD_STREET
            };
        }
    }
}
