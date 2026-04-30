package com.example.campusparkingbackend.controller;

import com.example.campusparkingbackend.entity.ParkingRecord;
import com.example.campusparkingbackend.entity.Vehicle;
import com.example.campusparkingbackend.repository.ParkingRecordRepository;
import com.example.campusparkingbackend.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class DatabaseFixController {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ParkingRecordRepository parkingRecordRepository;

    /**
     * 检查车辆的外键约束问题
     */
    @GetMapping("/check-vehicle-constraints/{vehicleId}")
    public ResponseEntity<Map<String, Object>> checkVehicleConstraints(@PathVariable Long vehicleId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查车辆是否存在
            Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
            if (vehicle == null) {
                result.put("error", "车辆不存在");
                return ResponseEntity.badRequest().body(result);
            }

            result.put("vehicle", vehicle.getPlateNumber());

            // 检查关联的停车记录
            List<ParkingRecord> records = parkingRecordRepository.findByVehicleId(vehicleId);
            result.put("parkingRecordsCount", records.size());
            result.put("parkingRecords", records.stream()
                    .map(r -> Map.of(
                            "id", r.getId(),
                            "status", r.getStatus(),
                            "startTime", r.getStartTime()
                    )).toList());

            // 检查是否有进行中的停车
            long activeParking = records.stream()
                    .filter(r -> r.getStatus() == ParkingRecord.ParkingStatus.PARKING)
                    .count();
            result.put("activeParkingCount", activeParking);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 强制删除车辆及其关联记录（仅用于调试）
     */
    @Transactional
    @DeleteMapping("/force-delete-vehicle/{vehicleId}")
    public ResponseEntity<Map<String, Object>> forceDeleteVehicle(@PathVariable Long vehicleId) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("🔧 强制删除车辆: " + vehicleId);

            // 1. 删除所有关联的停车记录
            List<ParkingRecord> records = parkingRecordRepository.findByVehicleId(vehicleId);
            System.out.println("找到关联记录: " + records.size() + " 条");

            if (!records.isEmpty()) {
                // 使用原生SQL绕过外键约束
                String deleteSql = "DELETE FROM parking_records WHERE vehicle_id = " + vehicleId;
                System.out.println("执行SQL: " + deleteSql);

                // 需要配置 EntityManager 来执行原生SQL
                // 这里先使用逐条删除
                int deletedCount = 0;
                for (ParkingRecord record : records) {
                    try {
                        parkingRecordRepository.delete(record);
                        deletedCount++;
                    } catch (Exception e) {
                        System.err.println("无法删除记录 " + record.getId() + ": " + e.getMessage());
                    }
                }
                result.put("deletedRecords", deletedCount);
            }

            // 2. 删除车辆
            vehicleRepository.deleteById(vehicleId);
            result.put("deletedVehicle", true);
            result.put("message", "车辆删除成功");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 修复所有车辆的约束问题
     */
    @GetMapping("/fix-all-constraints")
    public ResponseEntity<Map<String, Object>> fixAllConstraints() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Vehicle> allVehicles = vehicleRepository.findAll();
            int fixedCount = 0;
            int errorCount = 0;

            for (Vehicle vehicle : allVehicles) {
                try {
                    // 检查是否有孤立的停车记录
                    List<ParkingRecord> records = parkingRecordRepository.findByVehicleId(vehicle.getId());

                    // 检查是否有记录指向不存在的车辆
                    for (ParkingRecord record : records) {
                        if (record.getVehicle() == null) {
                            // 修复：设置正确的车辆引用
                            record.setVehicle(vehicle);
                            parkingRecordRepository.save(record);
                            fixedCount++;
                        }
                    }

                } catch (Exception e) {
                    errorCount++;
                    System.err.println("修复车辆 " + vehicle.getId() + " 失败: " + e.getMessage());
                }
            }

            result.put("totalVehicles", allVehicles.size());
            result.put("fixedCount", fixedCount);
            result.put("errorCount", errorCount);
            result.put("message", "约束检查完成");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
