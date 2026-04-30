package com.example.campusparkingbackend.controller;

import com.example.campusparkingbackend.dto.ApiResponse;
import com.example.campusparkingbackend.entity.User;
import com.example.campusparkingbackend.entity.Vehicle;
import com.example.campusparkingbackend.repository.UserRepository;
import com.example.campusparkingbackend.service.TencentOcrService;
import com.example.campusparkingbackend.service.VehicleService;
import com.example.campusparkingbackend.service.ParkingRecordService;
import com.example.campusparkingbackend.util.JwtUtil;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/parking")
@CrossOrigin(origins = "*")
public class ParkingEntryController {

    @Autowired
    private TencentOcrService tencentOcrService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private ParkingRecordService parkingRecordService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

    // 原有接口（校外用户用）
    @PostMapping("/entry/ocr")
    public ApiResponse entryByOcr(@RequestParam("licenseImage") MultipartFile file) {
        try {
            String plateNumber = tencentOcrService.recognizeLicensePlate(file);
            return ApiResponse.success("识别成功", plateNumber);
        } catch (TencentCloudSDKException e) {
            return ApiResponse.error("腾讯云识别服务错误: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("系统错误: " + e.getMessage());
        }
    }

    // 新增：校内用户专用接口
    @PostMapping("/entry/ocr/campus")
    public ApiResponse verifyCampusVehicleByImage(
            @RequestParam("licenseImage") MultipartFile file,
            @RequestHeader("Authorization") String token) {

        try {
            // 1. 获取当前用户
            User currentUser = getCurrentUserFromToken(token);

            // 2. OCR识别车牌
            String plateNumber = tencentOcrService.recognizeLicensePlate(file);

            // 3. 验证车辆属于当前用户且已审核
            Vehicle vehicle = vehicleService.getVehicleByPlateNumber(plateNumber)
                    .orElse(null);

            if (vehicle == null) {
                return ApiResponse.error("未找到该车牌号的注册车辆");
            }

            // 验证车辆归属
            if (!vehicle.getUser().getId().equals(currentUser.getId())) {
                return ApiResponse.error("该车辆不属于您");
            }

            // 验证车辆状态
            if (vehicle.getStatus() != Vehicle.VehicleStatus.APPROVED) {
                return ApiResponse.error("车辆未通过审核，请等待管理员审核");
            }

            // 4. 检查是否已在停车中
            boolean isParking = parkingRecordService.isVehicleParking(plateNumber);
            if (isParking) {
                return ApiResponse.error("该车辆已在停车中");
            }

            // 5. 返回车辆详细信息
            Map<String, Object> response = new HashMap<>();
            response.put("vehicle", vehicle);
            response.put("canPark", true);
            response.put("message", "车辆验证成功");

            return ApiResponse.success(response);

        } catch (Exception e) {
            return ApiResponse.error("验证失败: " + e.getMessage());
        }
    }

    // 从token获取用户的方法
    private User getCurrentUserFromToken(String token) {
        try {
            // 移除 "Bearer " 前缀
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            System.out.println("🔍 解析Token获取用户信息");
            System.out.println("Token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));

            // 使用正确的方法名：getUsernameFromToken
            String username = jwtUtil.getUsernameFromToken(token);
            System.out.println("✅ 提取的用户名: " + username);

            // 获取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);
            System.out.println("✅ 提取的用户ID: " + userId);

            // 获取用户角色（可选）
            String role = jwtUtil.getRoleFromToken(token);
            System.out.println("✅ 提取的用户角色: " + role);

            // 这里需要查询数据库获取完整的User对象
            // 使用 UserRepository 查询用户
            User user = null;
            try {
                if (userId != null) {
                    user = userRepository.findById(userId).orElse(null);
                    System.out.println("通过ID查询用户: " + (user != null ? "成功" : "失败"));
                }

                // 如果通过ID没找到，尝试通过用户名查找
                if (user == null && username != null) {
                    user = userRepository.findByUsername(username).orElse(null);
                    System.out.println("通过用户名查询用户: " + (user != null ? "成功" : "失败"));
                }
            } catch (Exception e) {
                System.err.println("查询用户失败: " + e.getMessage());
            }

            // 如果用户不存在，创建一个临时用户（仅用于开发测试）
            if (user == null) {
                System.out.println("⚠️ 用户不存在，创建临时用户对象");
                user = new User();
                user.setId(userId != null ? userId : 1L);
                user.setUsername(username != null ? username : "unknown_user");

                // 根据角色设置用户类型
                if (role != null) {
                    switch (role) {
                        case "ROLE_STUDENT":
                            user.setUserType(User.UserType.STUDENT);
                            break;
                        case "ROLE_TEACHER":
                            user.setUserType(User.UserType.TEACHER);
                            break;
                        case "ROLE_STAFF":
                            user.setUserType(User.UserType.STAFF);
                            break;
                        case "ROLE_ADMIN":
                            user.setUserType(User.UserType.ADMIN);
                            break;
                        case "ROLE_EXTERNAL_USER":
                            user.setUserType(User.UserType.EXTERNAL_USER);
                            break;
                        default:
                            user.setUserType(User.UserType.STUDENT); // 默认
                    }
                } else {
                    user.setUserType(User.UserType.STUDENT);
                }
            }

            System.out.println("✅ 返回用户: " + user.getUsername() + " (ID: " + user.getId() + ", 类型: " + user.getUserType() + ")");
            return user;

        } catch (Exception e) {
            System.err.println("❌ 获取用户信息失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("获取用户信息失败: " + e.getMessage());
        }
    }
}
