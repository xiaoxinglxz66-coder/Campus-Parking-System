package com.example.campusparkingbackend.controller;

import com.example.campusparkingbackend.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigurationController {

    @Autowired
    private ConfigurationService configurationService;

    @GetMapping
    public Map<String, Object> getSystemConfig() {
        return configurationService.getSystemConfig();
    }

    @PutMapping
    public void updateConfig(@RequestBody Map<String, Object> config) {
        config.forEach(configurationService::updateConfig);
    }
}
