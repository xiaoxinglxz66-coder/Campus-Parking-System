// src/main/java/com/example/campusparkingbackend/repository/ParkingSpotRepository.java
package com.example.campusparkingbackend.repository;

import com.example.campusparkingbackend.entity.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {

    // 根据状态查找停车位
    List<ParkingSpot> findByStatus(ParkingSpot.SpotStatus status);

    // 根据区域查找停车位
    List<ParkingSpot> findByZone(String zone);

    // 根据区域和状态查找
    List<ParkingSpot> findByZoneAndStatus(String zone, ParkingSpot.SpotStatus status);

    // 根据类型查找
    List<ParkingSpot> findBySpotType(ParkingSpot.SpotType spotType);

    // 🔥 关键：检查车位编号是否存在 - 必须添加
    boolean existsBySpotNumber(String spotNumber);

    // 根据车位编号查找
    Optional<ParkingSpot> findBySpotNumber(String spotNumber);

    // 修复查询语句 - 使用正确的表名和字段名
    @Query("SELECT p.zone, COUNT(p), SUM(CASE WHEN p.status = 'AVAILABLE' THEN 1 ELSE 0 END) " +
            "FROM ParkingSpot p GROUP BY p.zone ORDER BY p.zone")
    List<Object[]> countSpotsByZoneAndStatus();

    // 根据区域和状态统计数量
    long countByZoneAndStatus(String zone, ParkingSpot.SpotStatus status);

    // 根据区域统计总数
    long countByZone(String zone);
}
