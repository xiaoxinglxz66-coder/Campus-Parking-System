// src/main/java/com/example/campusparkingbackend/dto/VehicleDTO.java
package com.example.campusparkingbackend.dto;

import java.time.LocalDateTime;

public class VehicleDTO {
    private Long id;
    private String plateNumber;
    private String vehicleType;
    private String brand;
    private String color;
    private String status;
    private Boolean isTemporary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
    private String userName;
    private String userRealName;
    private String userType;

    // 默认构造器
    public VehicleDTO() {
    }

    // 全参数构造器
    public VehicleDTO(Long id, String plateNumber, String vehicleType, String brand,
                      String color, String status, Boolean isTemporary, LocalDateTime createdAt,
                      LocalDateTime updatedAt, Long userId, String userName, String userRealName, String userType) {
        this.id = id;
        this.plateNumber = plateNumber;
        this.vehicleType = vehicleType;
        this.brand = brand;
        this.color = color;
        this.status = status;
        this.isTemporary = isTemporary;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userId = userId;
        this.userName = userName;
        this.userRealName = userRealName;
        this.userType = userType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsTemporary() {
        return isTemporary;
    }

    public void setIsTemporary(Boolean isTemporary) {
        this.isTemporary = isTemporary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserRealName() {
        return userRealName;
    }

    public void setUserRealName(String userRealName) {
        this.userRealName = userRealName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    @Override
    public String toString() {
        return "VehicleDTO{" +
                "id=" + id +
                ", plateNumber='" + plateNumber + '\'' +
                ", vehicleType='" + vehicleType + '\'' +
                ", brand='" + brand + '\'' +
                ", color='" + color + '\'' +
                ", status='" + status + '\'' +
                ", isTemporary=" + isTemporary +
                ", createdAt=" + createdAt +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", userRealName='" + userRealName + '\'' +
                ", userType='" + userType + '\'' +
                '}';
    }
}
