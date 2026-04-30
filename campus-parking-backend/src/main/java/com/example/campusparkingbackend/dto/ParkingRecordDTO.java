// src/main/java/com/example/campusparkingbackend/dto/ParkingRecordDTO.java
package com.example.campusparkingbackend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ParkingRecordDTO {
    private Long id;
    private String plateNumber;
    private String userName;
    private String userType;
    private String parkingLotName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal fee;
    private String status;

    // 默认构造器
    public ParkingRecordDTO() {}

    // 全参数构造器
    public ParkingRecordDTO(Long id, String plateNumber, String userName, String userType,
                            String parkingLotName, LocalDateTime startTime, LocalDateTime endTime,
                            BigDecimal fee, String status) {
        this.id = id;
        this.plateNumber = plateNumber;
        this.userName = userName;
        this.userType = userType;
        this.parkingLotName = parkingLotName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.fee = fee;
        this.status = status;
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getParkingLotName() { return parkingLotName; }
    public void setParkingLotName(String parkingLotName) { this.parkingLotName = parkingLotName; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
