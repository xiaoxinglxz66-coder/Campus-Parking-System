// src/main/java/com/example/campusparkingbackend/service/DataCleanupService.java
package com.example.campusparkingbackend.service;

import com.example.campusparkingbackend.entity.ParkingSpot;
import com.example.campusparkingbackend.repository.ParkingRecordRepository;
import com.example.campusparkingbackend.repository.ParkingSpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DataCleanupService {

    @Autowired
    private ParkingRecordRepository parkingRecordRepository;

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;

    /**
     * 清理所有历史停车记录（保留今日及以后的记录）
     */
    @Transactional
    public void cleanupHistoricalParkingRecords() {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);

        // 删除今天之前的所有记录
        long deletedCount = parkingRecordRepository.deleteByStartTimeBefore(todayStart);
        System.out.println("✅ 已清理历史停车记录: " + deletedCount + " 条");

        // 重置所有停车位状态为可用
        resetAllParkingSpots();
    }

    /**
     * 重置所有停车位状态为可用
     */
    private void resetAllParkingSpots() {
        Iterable<ParkingSpot> allSpots = parkingSpotRepository.findAll();
        for (ParkingSpot spot : allSpots) {
            if (spot.getStatus() != ParkingSpot.SpotStatus.MAINTENANCE) {
                spot.setStatus(ParkingSpot.SpotStatus.AVAILABLE);
            }
        }
        parkingSpotRepository.saveAll(allSpots);
        System.out.println("✅ 已重置所有停车位状态为可用");
    }
}
