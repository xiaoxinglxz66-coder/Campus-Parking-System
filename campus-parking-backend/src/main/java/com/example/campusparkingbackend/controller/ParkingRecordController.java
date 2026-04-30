package com.example.campusparkingbackend.controller;

import com.example.campusparkingbackend.entity.ParkingRecord;
import com.example.campusparkingbackend.entity.ParkingSpot;
import com.example.campusparkingbackend.entity.User;
import com.example.campusparkingbackend.entity.Vehicle;
import com.example.campusparkingbackend.dto.ApiResponse;
import com.example.campusparkingbackend.repository.UserRepository;
import com.example.campusparkingbackend.service.*;
import com.example.campusparkingbackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/parking-records")
@CrossOrigin(origins = "*")
public class ParkingRecordController {

    @Autowired
    private ParkingRecordService parkingRecordService;
    @Autowired
    private ParkingSpotService parkingSpotService;
    @Autowired
    private UserService userService;
    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private DataCleanupService dataCleanupService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // ========== 实时停车接口 ==========

    /**
     * 实时开始停车
     */
    @PostMapping("/real-time/start")
    public ResponseEntity<?> realTimeStartParking(@RequestBody StartParkingRequest request,
                                                  @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            ParkingRecord record = parkingRecordService.realTimeStartParking(userId, request.getVehicleId(), request.getSpotId());
            return ResponseEntity.ok(new ApiResponse(true, "实时停车开始成功", new ParkingRecordDTO(record)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * 实时结束停车
     */
    @PostMapping("/real-time/{recordId}/end")
    public ResponseEntity<?> realTimeEndParking(@PathVariable Long recordId,
                                                @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            ParkingRecord record = parkingRecordService.realTimeEndParking(recordId, userId);
            return ResponseEntity.ok(new ApiResponse(true, "实时停车结束成功", new ParkingRecordDTO(record)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * 获取实时停车数据
     */
    @GetMapping("/real-time/data")
    public ResponseEntity<?> getRealTimeParkingData(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }

            ParkingRecordService.RealTimeParkingData realTimeData = parkingRecordService.getRealTimeParkingData();
            return ResponseEntity.ok(new ApiResponse(true, "获取实时数据成功", realTimeData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取实时数据失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取实时停车记录
     */
    @GetMapping("/real-time/records")
    public ResponseEntity<?> getRealTimeParkingRecords(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }

            List<ParkingRecord> records = parkingRecordService.getRealTimeParkingRecords();
            List<ParkingRecordDTO> recordDTOs = records.stream()
                    .map(ParkingRecordDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse(true, "获取实时记录成功", recordDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取实时记录失败: " + e.getMessage(), null));
        }
    }

    // ========== 管理员专用接口 ==========

    /**
     * 清理历史数据（管理员）
     */
    @PostMapping("/admin/cleanup-historical")
    public ResponseEntity<?> cleanupHistoricalData(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }

            dataCleanupService.cleanupHistoricalParkingRecords();
            return ResponseEntity.ok(new ApiResponse(true, "历史数据清理完成", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "数据清理失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取所有停车记录（管理员）- 支持分页和筛选
     */
    @GetMapping("/admin/page")
    public ResponseEntity<?> getAllParkingRecordsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String parkingLot,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String search,
            @RequestHeader("Authorization") String token) {

        try {
            System.out.println("👑 管理员分页查询停车记录...");

            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }

            Page<ParkingRecord> recordsPage = parkingRecordService.getAllParkingRecordsWithFilters(
                    page, size, parkingLot, status, userType, search);

            // 关键修改：使用Service层已经修复过的记录字段
            List<ParkingRecordDTO> recordDTOs = recordsPage.getContent().stream()
                    .map(record -> {
                        // 直接使用record中的字段构建DTO，不尝试访问user对象
                        ParkingRecordDTO dto = new ParkingRecordDTO();
                        dto.setId(record.getId());
                        dto.setPlateNumber(record.getPlateNumber());
                        dto.setParkingLotName(record.getParkingLotName());
                        dto.setStartTime(record.getStartTime());
                        dto.setEndTime(record.getEndTime());
                        dto.setFee(record.getFee() != null ? record.getFee().doubleValue() : null);
                        dto.setStatus(record.getStatus().name());

                        // 这些字段应该已经在Service层被正确设置了
                        dto.setUserName(record.getUserName() != null ? record.getUserName() : "未知用户");
                        dto.setUserType(record.getUserType() != null ? record.getUserType() : "UNKNOWN");

                        return dto;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("records", recordDTOs);
            responseData.put("total", recordsPage.getTotalElements());
            responseData.put("totalPages", recordsPage.getTotalPages());
            responseData.put("currentPage", recordsPage.getNumber() + 1);
            responseData.put("pageSize", recordsPage.getSize());

            return ResponseEntity.ok(new ApiResponse(true, "获取成功", responseData));

        } catch (Exception e) {
            System.err.println("❌ 获取停车记录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取所有停车记录（管理员）- 不分页
     */
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllParkingRecords(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }

            List<ParkingRecord> records = parkingRecordService.getAllParkingRecords();

            List<ParkingRecordDTO> recordDTOs = records.stream()
                    .map(ParkingRecordDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse(true, "获取成功", recordDTOs));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 根据状态获取停车记录（管理员）
     */
    @GetMapping("/admin/status/{status}")
    public ResponseEntity<?> getParkingRecordsByStatus(@PathVariable String status,
                                                       @RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }

            ParkingRecord.ParkingStatus parkingStatus;
            try {
                parkingStatus = ParkingRecord.ParkingStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "状态参数错误", null));
            }

            List<ParkingRecord> records = parkingRecordService.getParkingRecordsByStatus(parkingStatus);

            List<ParkingRecordDTO> recordDTOs = records.stream()
                    .map(ParkingRecordDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse(true, "获取成功", recordDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 根据停车位获取停车记录（管理员）
     */
    @GetMapping("/admin/spot/{spotId}")
    public ResponseEntity<?> getParkingRecordsBySpot(@PathVariable Long spotId,
                                                     @RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }
            List<ParkingRecord> records = parkingRecordService.getParkingRecordsBySpot(spotId);

            List<ParkingRecordDTO> recordDTOs = records.stream()
                    .map(ParkingRecordDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse(true, "获取成功", recordDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取今日停车记录（管理员）
     */
    @GetMapping("/admin/today")
    public ResponseEntity<?> getTodayParkingRecords(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }
            List<ParkingRecord> records = parkingRecordService.getTodayParkingRecords();

            List<ParkingRecordDTO> recordDTOs = records.stream()
                    .map(ParkingRecordDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse(true, "获取成功", recordDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取停车统计信息（管理员）
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<?> getParkingStatistics(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }
            ParkingRecordService.ParkingRecordStatistics stats = parkingRecordService.getParkingStatistics();
            return ResponseEntity.ok(new ApiResponse(true, "获取成功", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 根据时间范围搜索停车记录（管理员）
     */
    @GetMapping("/admin/search")
    public ResponseEntity<?> searchParkingRecords(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }
            List<ParkingRecord> records = parkingRecordService.getParkingRecordsByTimeRange(startTime, endTime);

            List<ParkingRecordDTO> recordDTOs = records.stream()
                    .map(ParkingRecordDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse(true, "获取成功", recordDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 根据停车场名称获取停车记录（管理员）
     */
    @GetMapping("/admin/parking-lot/{parkingLotName}")
    public ResponseEntity<?> getParkingRecordsByParkingLot(@PathVariable String parkingLotName,
                                                           @RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }
            List<ParkingRecord> records = parkingRecordService.getParkingRecordsByParkingLot(parkingLotName);

            List<ParkingRecordDTO> recordDTOs = records.stream()
                    .map(ParkingRecordDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse(true, "获取成功", recordDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取校内用户停车记录（管理员）
     */
    @GetMapping("/admin/campus-users")
    public ResponseEntity<?> getCampusUserParkingRecords(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }
            List<ParkingRecord> records = parkingRecordService.getCampusUserParkingRecords();

            List<ParkingRecordDTO> recordDTOs = records.stream()
                    .map(ParkingRecordDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse(true, "获取成功", recordDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取校外用户停车记录（管理员）
     */
    @GetMapping("/admin/external-users")
    public ResponseEntity<?> getExternalUserParkingRecords(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }
            List<ParkingRecord> records = parkingRecordService.getExternalUserParkingRecords();

            List<ParkingRecordDTO> recordDTOs = records.stream()
                    .map(ParkingRecordDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse(true, "获取成功", recordDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取所有停车场列表（管理员）
     */
    @GetMapping("/admin/parking-lots")
    public ResponseEntity<?> getAllParkingLots(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }
            List<String> parkingLots = parkingRecordService.getAllParkingLotNames();
            return ResponseEntity.ok(new ApiResponse(true, "获取成功", parkingLots));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    // ========== 校内用户专用接口 ==========

    /**
     * 校内用户：获取自己的停车记录（分页）
     */
    @GetMapping("/campus/my-records/page")
    public ResponseEntity<?> getCampusUserParkingRecordsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String parkingLot,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestHeader("Authorization") String token) {

        try {
            System.out.println("🎓 校内用户分页查询自己的停车记录");
            System.out.println("📊 page=" + page + ", size=" + size);
            System.out.println("🔍 筛选条件: parkingLot=" + parkingLot + ", status=" + status + ", search=" + search);

            // 验证Token并获取用户信息
            String cleanToken = cleanToken(token);
            String role = jwtUtil.getRoleFromToken(cleanToken);

            // 验证是否为校内用户
            if (!isCampusUser(role)) {
                System.err.println("❌ 非校内用户访问校内用户接口");
                return ResponseEntity.status(403).body(new ApiResponse(false, "仅限校内用户访问", null));
            }

            String username = jwtUtil.getUsernameFromToken(cleanToken);
            Long userId = userService.getUserIdByUsername(username);

            System.out.println("👤 校内用户: ID=" + userId + ", 用户名=" + username + ", 角色=" + role);

            // 调用Service获取该用户的停车记录
            Page<ParkingRecord> recordsPage = parkingRecordService.getUserParkingRecords(
                    userId, page, size, parkingLot, status, search);

            System.out.println("✅ 查询到 " + recordsPage.getTotalElements() + " 条记录");

            // 转换为DTO
            List<ParkingRecordDTO> recordDTOs = recordsPage.getContent().stream()
                    .map(ParkingRecordDTO::new)
                    .collect(Collectors.toList());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("records", recordDTOs);
            responseData.put("total", recordsPage.getTotalElements());
            responseData.put("totalPages", recordsPage.getTotalPages());
            responseData.put("currentPage", recordsPage.getNumber() + 1);
            responseData.put("pageSize", recordsPage.getSize());

            return ResponseEntity.ok(new ApiResponse(true, "获取成功", responseData));

        } catch (Exception e) {
            System.err.println("❌ 获取校内用户停车记录失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 校内用户：获取可用的停车场列表
     */
    @GetMapping("/campus/parking-lots")
    public ResponseEntity<?> getCampusUserParkingLots(@RequestHeader("Authorization") String token) {
        try {
            System.out.println("🎓 校内用户请求停车场列表");

            // 验证Token
            String cleanToken = cleanToken(token);
            String role = jwtUtil.getRoleFromToken(cleanToken);

            // 允许校内用户访问
            if (!isCampusUser(role)) {
                System.err.println("❌ 非校内用户访问停车场列表");
                return ResponseEntity.status(403).body(new ApiResponse(false, "仅限校内用户访问", null));
            }

            // 获取所有停车场
            List<String> parkingLots = parkingRecordService.getAllParkingLotNames();

            System.out.println("✅ 返回 " + parkingLots.size() + " 个停车场");
            return ResponseEntity.ok(new ApiResponse(true, "获取成功", parkingLots));

        } catch (Exception e) {
            System.err.println("❌ 获取停车场列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 校内用户：获取自己的所有停车记录（不分页）
     */
    @GetMapping("/campus/my-records/all")
    public ResponseEntity<?> getCampusUserAllParkingRecords(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String status) {

        try {
            System.out.println("🎓 校内用户请求所有停车记录");

            // 获取用户信息
            String cleanToken = cleanToken(token);
            String username = jwtUtil.getUsernameFromToken(cleanToken);
            Long userId = userService.getUserIdByUsername(username);

            List<ParkingRecord> records;
            if (status != null && !status.trim().isEmpty()) {
                try {
                    ParkingRecord.ParkingStatus parkingStatus =
                            ParkingRecord.ParkingStatus.valueOf(status.toUpperCase());
                    records = parkingRecordService.getUserParkingRecordsByStatus(userId, parkingStatus);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(new ApiResponse(false, "状态参数错误", null));
                }
            } else {
                records = parkingRecordService.getUserParkingRecords(userId);
            }

            List<ParkingRecordDTO> recordDTOs = records.stream()
                    .map(ParkingRecordDTO::new)
                    .collect(Collectors.toList());

            System.out.println("✅ 返回 " + recordDTOs.size() + " 条记录");
            return ResponseEntity.ok(new ApiResponse(true, "获取成功", recordDTOs));

        } catch (Exception e) {
            System.err.println("❌ 获取停车记录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 校内用户：获取自己当前的停车记录
     */
    @GetMapping("/campus/my-current")
    public ResponseEntity<?> getCampusUserCurrentParking(@RequestHeader("Authorization") String token) {
        try {
            String cleanToken = cleanToken(token);
            String username = jwtUtil.getUsernameFromToken(cleanToken);
            Long userId = userService.getUserIdByUsername(username);

            List<ParkingRecord> records = parkingRecordService.getUserCurrentParking(userId);

            List<ParkingRecordDTO> recordDTOs = records.stream()
                    .map(ParkingRecordDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse(true, "获取成功", recordDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 校内用户：获取用户在某停车场的记录
     */
    @GetMapping("/campus/my-records/{parkingLotName}")
    public ResponseEntity<?> getCampusUserParkingRecordsByLot(@PathVariable String parkingLotName,
                                                              @RequestHeader("Authorization") String token) {
        try {
            String cleanToken = cleanToken(token);
            String username = jwtUtil.getUsernameFromToken(cleanToken);
            Long userId = userService.getUserIdByUsername(username);

            List<ParkingRecord> records = parkingRecordService.getUserParkingRecordsByLot(userId, parkingLotName);

            List<ParkingRecordDTO> recordDTOs = records.stream()
                    .map(ParkingRecordDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse(true, "获取成功", recordDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    // ========== 校外用户专用接口 ==========

    /**
     * 校外用户：获取自己的停车记录（分页）
     */
    @GetMapping("/external/my-records/page")
    public ResponseEntity<?> getExternalUserParkingRecordsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String parkingLot,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestHeader("Authorization") String token) {

        try {
            System.out.println("👤 校外用户分页查询自己的停车记录");

            // 获取用户信息
            String cleanToken = cleanToken(token);
            String username = jwtUtil.getUsernameFromToken(cleanToken);
            Long userId = userService.getUserIdByUsername(username);

            // 调用Service获取该用户的停车记录
            Page<ParkingRecord> recordsPage = parkingRecordService.getUserParkingRecords(
                    userId, page, size, parkingLot, status, search);

            List<ParkingRecordDTO> recordDTOs = recordsPage.getContent().stream()
                    .map(ParkingRecordDTO::new)
                    .collect(Collectors.toList());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("records", recordDTOs);
            responseData.put("total", recordsPage.getTotalElements());
            responseData.put("totalPages", recordsPage.getTotalPages());
            responseData.put("currentPage", recordsPage.getNumber() + 1);
            responseData.put("pageSize", recordsPage.getSize());

            return ResponseEntity.ok(new ApiResponse(true, "获取成功", responseData));

        } catch (Exception e) {
            System.err.println("❌ 获取校外用户停车记录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 校外用户：获取自己的所有停车记录（不分页）
     */
    @GetMapping("/external/my-records/all")
    public ResponseEntity<?> getExternalUserAllParkingRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String token) {
        try {
            String cleanToken = cleanToken(token);
            String username = jwtUtil.getUsernameFromToken(cleanToken);
            Long userId = userService.getUserIdByUsername(username);

            List<ParkingRecord> records = parkingRecordService.getExternalUserParkingRecords(userId, page, size);

            List<ParkingRecordDTO> recordDTOs = records.stream()
                    .map(ParkingRecordDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse(true, "获取成功", recordDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 校外用户临时停车
     */
    @PostMapping("/external/temp-parking")
    public ResponseEntity<?> externalTempParking(@RequestBody ExternalTempParkingRequest request,
                                                 @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            ParkingRecord record = parkingRecordService.externalTempParking(userId, request.getPlateNumber(), request.getSpotId());
            return ResponseEntity.ok(new ApiResponse(true, "停车开始成功", new ParkingRecordDTO(record)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * 获取校外用户统计数据
     */
    @GetMapping("/external/statistics")
    public ResponseEntity<?> getExternalUserStatistics(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            ParkingRecordService.ExternalUserStatistics stats = parkingRecordService.getExternalUserStatistics(userId);
            return ResponseEntity.ok(new ApiResponse(true, "获取成功", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    // ========== 通用用户接口 ==========

    /**
     * 开始停车（兼容旧版本）
     */
    @PostMapping("/start")
    public ResponseEntity<?> startParking(@RequestBody StartParkingRequest request,
                                          @RequestHeader("Authorization") String token) {
        return realTimeStartParking(request, token);
    }

    /**
     * 结束停车（兼容旧版本）
     */
    @PostMapping("/{recordId}/end")
    public ResponseEntity<?> endParking(@PathVariable Long recordId,
                                        @RequestHeader("Authorization") String token) {
        return realTimeEndParking(recordId, token);
    }

    /**
     * 取消停车
     */
    @PostMapping("/{recordId}/cancel")
    public ResponseEntity<?> cancelParking(@PathVariable Long recordId,
                                           @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            ParkingRecord record = parkingRecordService.cancelParking(recordId, userId);
            return ResponseEntity.ok(new ApiResponse(true, "停车记录已取消", new ParkingRecordDTO(record)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * 获取用户自己的停车记录（通用）
     */
    @GetMapping("/my-records")
    public ResponseEntity<?> getMyParkingRecords(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            List<ParkingRecord> records = parkingRecordService.getUserParkingRecords(userId);

            List<ParkingRecordDTO> recordDTOs = records.stream()
                    .map(ParkingRecordDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse(true, "获取成功", recordDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取用户当前的停车记录（通用）
     */
    @GetMapping("/my-current")
    public ResponseEntity<?> getMyCurrentParking(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            List<ParkingRecord> records = parkingRecordService.getUserCurrentParking(userId);

            List<ParkingRecordDTO> recordDTOs = records.stream()
                    .map(ParkingRecordDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse(true, "获取成功", recordDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 根据ID获取停车记录（通用）
     */
    @GetMapping("/{recordId}")
    public ResponseEntity<?> getParkingRecordById(@PathVariable Long recordId) {
        try {
            ParkingRecord record = parkingRecordService.getParkingRecordById(recordId);
            if (record != null) {
                return ResponseEntity.ok(new ApiResponse(true, "获取成功", new ParkingRecordDTO(record)));
            } else {
                return ResponseEntity.ok(new ApiResponse(false, "记录不存在", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 根据车辆获取停车记录（通用）
     */
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<?> getParkingRecordsByVehicle(@PathVariable Long vehicleId,
                                                        @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            List<ParkingRecord> records = parkingRecordService.getParkingRecordsByVehicleAndUser(vehicleId, userId);

            List<ParkingRecordDTO> recordDTOs = records.stream()
                    .map(ParkingRecordDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse(true, "获取成功", recordDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    // ========== 财务管理接口 ==========

    /**
     * 获取财务概览数据（管理员）
     */
    @GetMapping("/finance/overview")
    public ResponseEntity<?> getFinanceOverview(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }

            FinanceOverviewData overview = parkingRecordService.getFinanceOverview();
            return ResponseEntity.ok(new ApiResponse(true, "获取财务概览成功", overview));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取财务概览失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取收入趋势数据（12个月）（管理员）
     */
    @GetMapping("/finance/revenue-trend")
    public ResponseEntity<?> getRevenueTrend(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }

            List<MonthlyRevenue> trendData = parkingRecordService.getRevenueTrend();
            return ResponseEntity.ok(new ApiResponse(true, "获取收入趋势成功", trendData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取收入趋势失败: " + e.getMessage(), null));
        }
    }

    /**
     * 分页查询校外用户收费记录（按时间倒序）（管理员）
     */
    @PostMapping("/finance/external-records")
    public ResponseEntity<?> getExternalUserRecordsWithPagination(
            @RequestBody FinanceRecordQueryRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "权限不足", null));
            }

            Page<ParkingRecord> recordsPage = parkingRecordService.getExternalUserRecordsWithPagination(
                    request.getPlateNumber(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getStatus(),
                    request.getPage(),
                    request.getSize()
            );

            List<ParkingRecordDTO> recordDTOs = recordsPage.getContent().stream()
                    .map(ParkingRecordDTO::new)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("records", recordDTOs);
            response.put("total", recordsPage.getTotalElements());
            response.put("totalPages", recordsPage.getTotalPages());
            response.put("currentPage", recordsPage.getNumber());

            return ResponseEntity.ok(new ApiResponse(true, "获取收费记录成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取收费记录失败: " + e.getMessage(), null));
        }
    }
    // ParkingRecordController.java - 新增
    @PostMapping("/campus/quick-park")
    public ApiResponse campusQuickPark(
            @RequestParam("vehicleId") Long vehicleId,
            @RequestParam(value = "spotId", required = false) Long spotId,
            @RequestHeader("Authorization") String token) {

        try {
            // 1. 获取当前用户
            User currentUser = getCurrentUserFromToken(token);

            // 2. 验证车辆归属
            Vehicle vehicle = vehicleService.getVehicleById(vehicleId)
                    .orElseThrow(() -> new RuntimeException("车辆不存在"));

            if (!vehicle.getUser().getId().equals(currentUser.getId())) {
                return ApiResponse.error("车辆不属于当前用户");
            }

            // 3. 验证车辆状态
            if (vehicle.getStatus() != Vehicle.VehicleStatus.APPROVED) {
                return ApiResponse.error("车辆未通过审核");
            }

            // 4. 检查是否已在停车中
            boolean isParking = parkingRecordService.isVehicleParking(vehicleId);
            if (isParking) {
                return ApiResponse.error("该车辆已在停车中");
            }

            // 5. 处理停车位
            ParkingSpot spot;
            if (spotId != null) {
                spot = parkingSpotService.getParkingSpotById(spotId)
                        .orElseThrow(() -> new RuntimeException("停车位不存在"));

                if (spot.getStatus() != ParkingSpot.SpotStatus.AVAILABLE) {
                    return ApiResponse.error("该停车位不可用");
                }
            } else {
                // 自动分配停车位
                spot = parkingSpotService.findAvailableSpot();
                if (spot == null) {
                    return ApiResponse.error("当前没有可用停车位");
                }
            }

            // 6. 开始停车（校内用户免费）
            ParkingRecord record = parkingRecordService.startParking(
                    currentUser.getId(),
                    vehicleId,
                    spot.getId()
            );

            return ApiResponse.success("校内用户停车开始成功", record);

        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("停车开始失败: " + e.getMessage());
        }
    }

    // 从token获取用户的方法
    private User getCurrentUserFromToken(String token) {
        try {
            // 移除 "Bearer " 前缀
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 使用正确的方法名
            String username = jwtUtil.getUsernameFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);

            // 优先通过ID查询用户
            User user = null;
            if (userId != null) {
                Optional<User> userOptional = userRepository.findById(userId);
                if (userOptional.isPresent()) {
                    user = userOptional.get();
                    return user;
                }
            }

            // 如果ID查询失败，尝试通过用户名查询
            if (user == null && username != null) {
                Optional<User> userOptional = userRepository.findByUsername(username);
                if (userOptional.isPresent()) {
                    return userOptional.get();
                }
            }

            // 用户不存在，抛出异常
            throw new RuntimeException("用户不存在，请重新登录");

        } catch (Exception e) {
            throw new RuntimeException("获取用户信息失败: " + e.getMessage());
        }
    }
    // ========== 工具方法 ==========
// ========== 统一的权限验证方法 ==========

    /**
     * 验证用户是否有权访问该路径
     */
    private void validateAccess(String token, String path) throws AccessDeniedException {
        String cleanToken = cleanToken(token);
        String role = jwtUtil.getRoleFromToken(cleanToken);

        System.out.println("🔐 权限验证: 用户角色=" + role + ", 访问路径=" + path);

        // 路径包含 /admin/ 必须是管理员
        if (path.contains("/admin/") && !"ADMIN".equals(role)) {
            System.err.println("❌ 权限拒绝: 非管理员访问管理员接口");
            throw new AccessDeniedException("权限不足：仅限管理员访问");
        }

        // 路径包含 /campus/ 必须是校内用户
        if (path.contains("/campus/") && !isCampusUser(role)) {
            System.err.println("❌ 权限拒绝: 非校内用户访问校内接口");
            throw new AccessDeniedException("权限不足：仅限校内用户访问");
        }

        // 路径包含 /external/ 必须是校外用户
        if (path.contains("/external/") && !"EXTERNAL_USER".equals(role)) {
            System.err.println("❌ 权限拒绝: 非校外用户访问校外接口");
            throw new AccessDeniedException("权限不足：仅限校外用户访问");
        }

        System.out.println("✅ 权限验证通过");
    }

    /**
     * 检查用户是否有管理权限
     */
    private boolean hasAdminAccess(String token) {
        try {
            String cleanToken = cleanToken(token);
            String role = jwtUtil.getRoleFromToken(cleanToken);
            return "ADMIN".equals(role);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查用户是否有校内用户权限
     */
    private boolean hasCampusAccess(String token) {
        try {
            String cleanToken = cleanToken(token);
            String role = jwtUtil.getRoleFromToken(cleanToken);
            return isCampusUser(role);
        } catch (Exception e) {
            return false;
        }
    }
    private Long getUserIdFromToken(String token) {
        String cleanToken = cleanToken(token);
        String username = jwtUtil.getUsernameFromToken(cleanToken);
        return userService.getUserIdByUsername(username);
    }

    private boolean isAdmin(String token) {
        try {
            System.out.println("🔍 ===== 开始管理员权限检查 =====");

            // 1. 基本检查
            if (token == null) {
                System.err.println("❌ Token为null");
                return false;
            }

            System.out.println("🔑 原始Token: " + token);

            // 2. 清理Token
            String cleanToken = cleanToken(token);
            System.out.println("🧹 清理后Token: " + cleanToken);

            // 3. 先验证Token是否有效
            System.out.println("🔄 验证Token有效性...");
            boolean isValid = jwtUtil.validateToken(cleanToken);
            System.out.println("✅ Token有效性: " + isValid);

            if (!isValid) {
                System.err.println("❌ Token无效，无法进行权限检查");
                return false;
            }

            // 4. 获取角色
            System.out.println("🔄 从Token获取角色...");
            String role = jwtUtil.getRoleFromToken(cleanToken);
            System.out.println("👤 获取到的角色: " + role);

            // 5. 检查是否为管理员
            boolean isAdmin = "ADMIN".equals(role);
            System.out.println("👑 是否为管理员: " + isAdmin);
            System.out.println("🔍 ===== 管理员权限检查结束 =====");

            return isAdmin;

        } catch (Exception e) {
            System.err.println("💥 isAdmin方法发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean isCampusUser(String role) {
        return "STUDENT".equals(role) || "TEACHER".equals(role) || "STAFF".equals(role);
    }

    private String cleanToken(String token) {
        return token.replace("Bearer ", "").trim();
    }

    // ========== 请求体和响应类 ==========

    public static class StartParkingRequest {
        private Long vehicleId;
        private Long spotId;

        public Long getVehicleId() { return vehicleId; }
        public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
        public Long getSpotId() { return spotId; }
        public void setSpotId(Long spotId) { this.spotId = spotId; }
    }

    public static class ExternalTempParkingRequest {
        private String plateNumber;
        private Long spotId;

        public String getPlateNumber() { return plateNumber; }
        public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
        public Long getSpotId() { return spotId; }
        public void setSpotId(Long spotId) { this.spotId = spotId; }
    }

    public static class FinanceRecordQueryRequest {
        private String plateNumber;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String status;
        private int page = 0;
        private int size = 10;

        public String getPlateNumber() { return plateNumber; }
        public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }

    public static class FinanceOverviewData {
        private final BigDecimal todayRevenue;
        private final BigDecimal monthRevenue;
        private final BigDecimal yearRevenue;
        private final long todayParkingCount;
        private final long currentParkingCount;

        public FinanceOverviewData(BigDecimal todayRevenue, BigDecimal monthRevenue,
                                   BigDecimal yearRevenue, long todayParkingCount, long currentParkingCount) {
            this.todayRevenue = todayRevenue;
            this.monthRevenue = monthRevenue;
            this.yearRevenue = yearRevenue;
            this.todayParkingCount = todayParkingCount;
            this.currentParkingCount = currentParkingCount;
        }

        public BigDecimal getTodayRevenue() { return todayRevenue; }
        public BigDecimal getMonthRevenue() { return monthRevenue; }
        public BigDecimal getYearRevenue() { return yearRevenue; }
        public long getTodayParkingCount() { return todayParkingCount; }
        public long getCurrentParkingCount() { return currentParkingCount; }
    }

    public static class MonthlyRevenue {
        private final String month;
        private final BigDecimal revenue;
        private final boolean isRealTime;

        public MonthlyRevenue(String month, BigDecimal revenue, boolean isRealTime) {
            this.month = month;
            this.revenue = revenue;
            this.isRealTime = isRealTime;
        }

        public String getMonth() { return month; }
        public BigDecimal getRevenue() { return revenue; }
        public boolean isRealTime() { return isRealTime; }
    }

    // 修改 ParkingRecordController 中的内部类 ParkingRecordDTO
    public static class ParkingRecordDTO {
        private Long id;
        private String plateNumber;
        private String parkingLotName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Double fee;
        private String status;
        private String userName;
        private String userType;
        private String userPhone; // 新增：显示用户电话（特别是校外用户）

        // 添加一个空的构造函数
        public ParkingRecordDTO() {}

        public ParkingRecordDTO(ParkingRecord record) {
            this.id = record.getId();
            this.plateNumber = record.getPlateNumber();
            this.parkingLotName = record.getParkingLotName();
            this.startTime = record.getStartTime();
            this.endTime = record.getEndTime();
            this.fee = record.getFee() != null ? record.getFee().doubleValue() : null;
            this.status = record.getStatus().name();

            // 关键修改：不再尝试访问 record.getUser()！
            // 直接使用record中已经存储的字段

            // 使用record中的userName字段（这个字段应该在创建记录时已经正确设置了）
            this.userName = record.getUserName() != null ? record.getUserName() : "未知用户";
            this.userType = record.getUserType() != null ? record.getUserType() : "UNKNOWN";

            // 注意：record中没有存储userPhone字段，所以我们需要从user对象获取
            // 但由于LazyInitializationException，我们暂时不获取这个字段
            this.userPhone = null; // 暂时设为null，或者从其他途径获取

            // 特殊处理：如果userName是"未知用户"且userType是校外用户，尝试优化显示
            if ("EXTERNAL_USER".equals(this.userType) && "未知用户".equals(this.userName)) {
                this.userName = "校外用户";
            }
        }

        // 添加一个从User对象构建DTO的方法（供Service层使用）
        public ParkingRecordDTO(ParkingRecord record, User user) {
            this.id = record.getId();
            this.plateNumber = record.getPlateNumber();
            this.parkingLotName = record.getParkingLotName();
            this.startTime = record.getStartTime();
            this.endTime = record.getEndTime();
            this.fee = record.getFee() != null ? record.getFee().doubleValue() : null;
            this.status = record.getStatus().name();

            // 如果提供了user对象，可以正确处理
            if (user != null) {
                if (user.getUserType() == User.UserType.EXTERNAL_USER) {
                    if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
                        this.userName = user.getPhone();
                    } else {
                        this.userName = user.getUsername() != null ? user.getUsername() : "校外用户";
                    }
                } else {
                    if (user.getRealName() != null && !user.getRealName().trim().isEmpty()) {
                        this.userName = user.getRealName();
                    } else {
                        this.userName = user.getUsername() != null ? user.getUsername() : "校内用户";
                    }
                }
                this.userType = user.getUserType().name();
                this.userPhone = user.getPhone();
            } else {
                // 回退到record中的字段
                this.userName = record.getUserName() != null ? record.getUserName() : "未知用户";
                this.userType = record.getUserType() != null ? record.getUserType() : "UNKNOWN";
                this.userPhone = null;
            }
        }

        // Getter和Setter
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getPlateNumber() { return plateNumber; }
        public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

        public String getParkingLotName() { return parkingLotName; }
        public void setParkingLotName(String parkingLotName) { this.parkingLotName = parkingLotName; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public Double getFee() { return fee; }
        public void setFee(Double fee) { this.fee = fee; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public String getUserType() { return userType; }
        public void setUserType(String userType) { this.userType = userType; }

        public String getUserPhone() { return userPhone; }
        public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    }
}
