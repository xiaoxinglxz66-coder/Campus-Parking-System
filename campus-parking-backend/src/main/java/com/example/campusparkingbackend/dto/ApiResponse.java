package com.example.campusparkingbackend.dto;

public class ApiResponse {
    // 核心字段：三选一即可。这里选择最通用的 success/message/data 结构
    private boolean success;
    private String message;
    private Object data;

    // 构造方法1：无参构造（供序列化框架如Jackson使用）
    public ApiResponse() {
    }

    // 构造方法2：全参构造（核心构造方法）
    public ApiResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // === 静态工厂方法（核心功能）===
    // 成功响应（自动设置 success=true）
    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(true, message, data);
    }

    // 失败响应（自动设置 success=false）
    public static ApiResponse error(String message) {
        return new ApiResponse(false, message, null);
    }

    // 可选：提供一个快速成功响应（当不需要特定消息时）
    public static ApiResponse success(Object data) {
        return new ApiResponse(true, "操作成功", data);
    }

    // === Getters and Setters（必须提供，供JSON序列化使用）===
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    // 可选：重写toString方法便于调试
    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
