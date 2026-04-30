package com.example.campusparkingbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ConfigurationService {

    @Value("${application.name:校园停车管理系统}")
    private String appName;

    @Value("${application.version:1.0.0}")
    private String appVersion;

    // 系统配置
    private final Map<String, Object> configurations = new HashMap<>();

    public ConfigurationService() {
        // 初始化默认配置
        configurations.put("parkingFeeRate", 5.00);
        configurations.put("maxParkingHours", 24);
        configurations.put("reservationTimeout", 30); // 分钟
        configurations.put("systemMaintenance", false);
    }

    public Map<String, Object> getSystemConfig() {
        Map<String, Object> config = new HashMap<>(configurations);
        config.put("appName", appName);
        config.put("appVersion", appVersion);
        return config;
    }

    public void updateConfig(String key, Object value) {
        configurations.put(key, value);
    }

    public Object getConfig(String key) {
        return configurations.get(key);
    }
}
