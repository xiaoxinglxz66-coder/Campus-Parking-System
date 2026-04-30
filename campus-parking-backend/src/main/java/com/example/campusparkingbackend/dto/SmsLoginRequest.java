package com.example.campusparkingbackend.dto;

public class SmsLoginRequest {
    private String phone;
    private String code;

    // 默认构造函数
    public SmsLoginRequest() {}

    // 全参构造函数
    public SmsLoginRequest(String phone, String code) {
        this.phone = phone;
        this.code = code;
    }

    // Getters and Setters
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
