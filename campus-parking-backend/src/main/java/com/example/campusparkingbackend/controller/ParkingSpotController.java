package com.example.campusparkingbackend.controller;

import com.example.campusparkingbackend.entity.ParkingSpot;
import com.example.campusparkingbackend.service.ParkingSpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/parking-spots")
public class ParkingSpotController {

    @Autowired
    private ParkingSpotService parkingSpotService;

    // 获取所有停车位
    @GetMapping
    public List<ParkingSpot> getAllParkingSpots() {
        return parkingSpotService.getAllParkingSpots();
    }

    // 根据ID获取停车位
    @GetMapping("/{id}")
    public ResponseEntity<ParkingSpot> getParkingSpotById(@PathVariable Long id) {
        Optional<ParkingSpot> parkingSpot = parkingSpotService.getParkingSpotById(id);
        return parkingSpot.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 创建停车位 - 修复版本：处理各种数据类型
    @PostMapping
    public ResponseEntity<?> createParkingSpot(@RequestBody Map<String, Object> request) {
        try {
            ParkingSpot parkingSpot = new ParkingSpot();

            // 🔥 处理spotNumber
            if (request.get("spotNumber") == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "车位编号不能为空"));
            }
            parkingSpot.setSpotNumber(request.get("spotNumber").toString().trim());

            // 🔥 处理zone
            if (request.get("zone") == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "区域不能为空"));
            }
            parkingSpot.setZone(request.get("zone").toString().trim());

            // 🔥 处理spotType
            if (request.get("spotType") != null) {
                try {
                    parkingSpot.setSpotType(ParkingSpot.SpotType.valueOf(
                            request.get("spotType").toString().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    parkingSpot.setSpotType(ParkingSpot.SpotType.REGULAR);
                }
            }

            // 🔥 处理status
            if (request.get("status") != null) {
                try {
                    parkingSpot.setStatus(ParkingSpot.SpotStatus.valueOf(
                            request.get("status").toString().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    parkingSpot.setStatus(ParkingSpot.SpotStatus.AVAILABLE);
                }
            }

            // 🔥 关键：处理hourlyRate（处理字符串和数字）
            if (request.get("hourlyRate") != null) {
                try {
                    Object rate = request.get("hourlyRate");
                    BigDecimal hourlyRate;

                    if (rate instanceof Number) {
                        hourlyRate = new BigDecimal(rate.toString());
                    } else if (rate instanceof String) {
                        hourlyRate = new BigDecimal((String) rate);
                    } else {
                        hourlyRate = new BigDecimal("5.00");
                    }

                    // 验证费率是否有效
                    if (hourlyRate.compareTo(BigDecimal.ZERO) < 0) {
                        hourlyRate = new BigDecimal("5.00");
                    }

                    parkingSpot.setHourlyRate(hourlyRate);
                } catch (Exception e) {
                    parkingSpot.setHourlyRate(new BigDecimal("5.00"));
                }
            }

            // 🔥 处理location
            if (request.get("location") != null) {
                parkingSpot.setLocation(request.get("location").toString().trim());
            }

            ParkingSpot createdSpot = parkingSpotService.createParkingSpot(parkingSpot);
            return ResponseEntity.ok(createdSpot);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "创建停车位失败: " + e.getMessage()));
        }
    }

    // 更新停车位信息 - 兼容两种传参方式
    @PutMapping("/{id}")
    public ResponseEntity<?> updateParkingSpot(@PathVariable Long id, @RequestBody Object request) {
        try {
            ParkingSpot parkingSpotDetails = new ParkingSpot();

            // 判断是Map还是ParkingSpot对象
            if (request instanceof Map) {
                Map<String, Object> requestMap = (Map<String, Object>) request;

                if (requestMap.get("spotNumber") != null) {
                    parkingSpotDetails.setSpotNumber(requestMap.get("spotNumber").toString().trim());
                }
                if (requestMap.get("zone") != null) {
                    parkingSpotDetails.setZone(requestMap.get("zone").toString().trim());
                }
                if (requestMap.get("spotType") != null) {
                    try {
                        parkingSpotDetails.setSpotType(ParkingSpot.SpotType.valueOf(
                                requestMap.get("spotType").toString().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        // 使用默认值
                    }
                }
                if (requestMap.get("status") != null) {
                    try {
                        parkingSpotDetails.setStatus(ParkingSpot.SpotStatus.valueOf(
                                requestMap.get("status").toString().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        // 使用默认值
                    }
                }
                if (requestMap.get("hourlyRate") != null) {
                    try {
                        Object rate = requestMap.get("hourlyRate");
                        if (rate instanceof Number) {
                            parkingSpotDetails.setHourlyRate(new BigDecimal(rate.toString()));
                        } else if (rate instanceof String) {
                            parkingSpotDetails.setHourlyRate(new BigDecimal((String) rate));
                        }
                    } catch (Exception e) {
                        // 忽略错误，不更新费率
                    }
                }
                if (requestMap.get("location") != null) {
                    parkingSpotDetails.setLocation(requestMap.get("location").toString().trim());
                }

            } else if (request instanceof ParkingSpot) {
                parkingSpotDetails = (ParkingSpot) request;
            }

            ParkingSpot updatedSpot = parkingSpotService.updateParkingSpot(id, parkingSpotDetails);
            return ResponseEntity.ok(updatedSpot);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 更新停车位状态 - 修复后的版本
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateParkingSpotStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String statusStr = request.get("status");
            if (statusStr == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "状态不能为空"));
            }

            ParkingSpot.SpotStatus status;
            try {
                status = ParkingSpot.SpotStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "无效的状态值，可选值：AVAILABLE, OCCUPIED, MAINTENANCE"));
            }

            ParkingSpot updatedSpot = parkingSpotService.updateParkingSpotStatus(id, status);
            return ResponseEntity.ok(updatedSpot);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 删除停车位
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteParkingSpot(@PathVariable Long id) {
        try {
            parkingSpotService.deleteParkingSpot(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 根据区域获取停车位
    @GetMapping("/zone/{zone}")
    public List<ParkingSpot> getParkingSpotsByZone(@PathVariable String zone) {
        return parkingSpotService.getParkingSpotsByZone(zone);
    }

    // 根据状态获取停车位
    @GetMapping("/status/{status}")
    public List<ParkingSpot> getParkingSpotsByStatus(@PathVariable String status) {
        try {
            ParkingSpot.SpotStatus spotStatus = ParkingSpot.SpotStatus.valueOf(status.toUpperCase());
            return parkingSpotService.getParkingSpotsByStatus(spotStatus);
        } catch (IllegalArgumentException e) {
            // 返回空列表而不是抛出异常
            return List.of();
        }
    }

    /**
     * 获取所有停车场统计信息
     */
    @GetMapping("/parking-lots/stats")
    public ResponseEntity<List<ParkingSpotService.ParkingLotStats>> getAllParkingLotStats() {
        try {
            List<ParkingSpotService.ParkingLotStats> stats = parkingSpotService.getAllParkingLotStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 获取停车场可用车位数量
     */
    @GetMapping("/parking-lot/{parkingLot}/available-count")
    public ResponseEntity<Long> getAvailableSpotsCount(@PathVariable String parkingLot) {
        try {
            long count = parkingSpotService.getAvailableSpotsCount(parkingLot);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(0L);
        }
    }

    /**
     * 获取停车场总车位数量
     */
    @GetMapping("/parking-lot/{parkingLot}/total-count")
    public ResponseEntity<Long> getTotalSpotsCount(@PathVariable String parkingLot) {
        try {
            long count = parkingSpotService.getTotalSpotsCount(parkingLot);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(0L);
        }
    }

    /**
     * 获取可用停车位
     */
    @GetMapping("/available")
    public ResponseEntity<List<ParkingSpot>> getAvailableSpots() {
        try {
            List<ParkingSpot> availableSpots = parkingSpotService.getAvailableSpots();
            return ResponseEntity.ok(availableSpots);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
