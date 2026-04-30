package com.example.campusparkingbackend.dto;

public class AuthResponse {
    private String token;
    private String username;
    private String userType;
    private String message;

    // 构造器
    public AuthResponse() {}

    public AuthResponse(String token, String username, String userType, String message) {
        this.token = token;
        this.username = username;
        this.userType = userType;
        this.message = message;
    }

    // Getter和Setter
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
