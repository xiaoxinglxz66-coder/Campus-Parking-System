package com.example.campusparkingbackend.service;

import com.example.campusparkingbackend.entity.ParkingSpot;
import com.example.campusparkingbackend.repository.ParkingSpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ParkingSpotService {

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;

    /**
     * 获取所有停车位
     */
    public List<ParkingSpot> getAllParkingSpots() {
        return parkingSpotRepository.findAll();
    }

    /**
     * 根据ID获取停车位
     */
    public Optional<ParkingSpot> getParkingSpotById(Long id) {
        return parkingSpotRepository.findById(id);
    }

    /**
     * 查找可用的停车位
     */
    public ParkingSpot findAvailableSpot() {
        List<ParkingSpot> availableSpots = parkingSpotRepository.findByStatus(ParkingSpot.SpotStatus.AVAILABLE);
        if (availableSpots.isEmpty()) {
            return null;
        }
        // 返回第一个可用的停车位
        return availableSpots.get(0);
    }

    /**
     * 创建停车位 - 关键修复点
     */
    @Transactional
    public ParkingSpot createParkingSpot(ParkingSpot parkingSpot) {
        // 🔥 检查必填字段
        if (parkingSpot.getSpotNumber() == null || parkingSpot.getSpotNumber().trim().isEmpty()) {
            throw new RuntimeException("车位编号不能为空");
        }
        if (parkingSpot.getZone() == null || parkingSpot.getZone().trim().isEmpty()) {
            throw new RuntimeException("区域不能为空");
        }

        // 清理数据
        String spotNumber = parkingSpot.getSpotNumber().trim();
        parkingSpot.setSpotNumber(spotNumber);
        parkingSpot.setZone(parkingSpot.getZone().trim());

        // 🔥 使用Repository的existsBySpotNumber方法
        if (parkingSpotRepository.existsBySpotNumber(spotNumber)) {
            throw new RuntimeException("车位编号 '" + spotNumber + "' 已存在");
        }

        // 设置默认值
        if (parkingSpot.getLocation() == null || parkingSpot.getLocation().trim().isEmpty()) {
            parkingSpot.setLocation("未指定");
        }

        if (parkingSpot.getHourlyRate() == null) {
            parkingSpot.setHourlyRate(new BigDecimal("5.00"));
        }

        if (parkingSpot.getSpotType() == null) {
            parkingSpot.setSpotType(ParkingSpot.SpotType.REGULAR);
        }

        if (parkingSpot.getStatus() == null) {
            parkingSpot.setStatus(ParkingSpot.SpotStatus.AVAILABLE);
        }

        // 保存并返回
        try {
            return parkingSpotRepository.save(parkingSpot);
        } catch (Exception e) {
            throw new RuntimeException("保存停车位失败: " + e.getMessage());
        }
    }

    /**
     * 更新停车位信息
     */
    @Transactional
    public ParkingSpot updateParkingSpot(Long id, ParkingSpot parkingSpotDetails) {
        ParkingSpot parkingSpot = parkingSpotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("停车位不存在"));

        // 更新spotNumber时检查是否重复
        if (parkingSpotDetails.getSpotNumber() != null &&
                !parkingSpotDetails.getSpotNumber().equals(parkingSpot.getSpotNumber())) {

            String newSpotNumber = parkingSpotDetails.getSpotNumber().trim();
            if (parkingSpotRepository.existsBySpotNumber(newSpotNumber)) {
                throw new RuntimeException("车位编号 '" + newSpotNumber + "' 已存在");
            }
            parkingSpot.setSpotNumber(newSpotNumber);
        }

        // 更新其他字段
        if (parkingSpotDetails.getZone() != null) {
            parkingSpot.setZone(parkingSpotDetails.getZone().trim());
        }

        if (parkingSpotDetails.getSpotType() != null) {
            parkingSpot.setSpotType(parkingSpotDetails.getSpotType());
        }

        if (parkingSpotDetails.getStatus() != null) {
            parkingSpot.setStatus(parkingSpotDetails.getStatus());
        }

        if (parkingSpotDetails.getHourlyRate() != null) {
            parkingSpot.setHourlyRate(parkingSpotDetails.getHourlyRate());
        }

        if (parkingSpotDetails.getLocation() != null) {
            parkingSpot.setLocation(parkingSpotDetails.getLocation().trim());
        }

        return parkingSpotRepository.save(parkingSpot);
    }

    /**
     * 更新停车位状态
     */
    @Transactional
    public ParkingSpot updateParkingSpotStatus(Long id, ParkingSpot.SpotStatus status) {
        ParkingSpot parkingSpot = parkingSpotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("停车位不存在"));
        parkingSpot.setStatus(status);
        return parkingSpotRepository.save(parkingSpot);
    }

    /**
     * 删除停车位
     */
    @Transactional
    public void deleteParkingSpot(Long id) {
        if (!parkingSpotRepository.existsById(id)) {
            throw new RuntimeException("停车位不存在");
        }
        parkingSpotRepository.deleteById(id);
    }

    /**
     * 根据区域获取停车位
     */
    public List<ParkingSpot> getParkingSpotsByZone(String zone) {
        return parkingSpotRepository.findByZone(zone);
    }

    /**
     * 根据状态获取停车位
     */
    public List<ParkingSpot> getParkingSpotsByStatus(ParkingSpot.SpotStatus status) {
        return parkingSpotRepository.findByStatus(status);
    }

    /**
     * 获取可用停车位
     */
    public List<ParkingSpot> getAvailableSpots() {
        return parkingSpotRepository.findByStatus(ParkingSpot.SpotStatus.AVAILABLE);
    }

    /**
     * 获取所有停车场统计信息
     */
    public List<ParkingLotStats> getAllParkingLotStats() {
        List<Object[]> results = parkingSpotRepository.countSpotsByZoneAndStatus();
        return results.stream()
                .map(result -> new ParkingLotStats(
                        (String) result[0],
                        ((Number) result[1]).longValue(),
                        ((Number) result[2]).longValue()
                ))
                .toList();
    }

    /**
     * 获取停车场可用车位数量
     */
    public long getAvailableSpotsCount(String parkingLot) {
        return parkingSpotRepository.countByZoneAndStatus(parkingLot, ParkingSpot.SpotStatus.AVAILABLE);
    }

    /**
     * 获取停车场总车位数量
     */
    public long getTotalSpotsCount(String parkingLot) {
        return parkingSpotRepository.countByZone(parkingLot);
    }

    /**
     * 停车场统计类
     */
    public static class ParkingLotStats {
        private final String zone;
        private final long totalSpots;
        private final long availableSpots;

        public ParkingLotStats(String zone, long totalSpots, long availableSpots) {
            this.zone = zone;
            this.totalSpots = totalSpots;
            this.availableSpots = availableSpots;
        }

        public String getZone() { return zone; }
        public long getTotalSpots() { return totalSpots; }
        public long getAvailableSpots() { return availableSpots; }
    }
}
