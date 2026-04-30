// src/main/java/com/example/campusparkingbackend/config/DataInitializer.java
package com.example.campusparkingbackend.config;

import com.example.campusparkingbackend.entity.ParkingSpot;
import com.example.campusparkingbackend.entity.User;
import com.example.campusparkingbackend.repository.ParkingSpotRepository;
import com.example.campusparkingbackend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ParkingSpotRepository parkingSpotRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.parkingSpotRepository = parkingSpotRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚗 === 校园停车管理系统数据初始化开始 ===");
        initializeAdminUser();
        initializeParkingSpots();
        System.out.println("✅ === 数据初始化完成 ===");
    }

    private void initializeAdminUser() {
        try {
            // 检查管理员是否已存在
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRealName("系统管理员");
                admin.setUserType(User.UserType.ADMIN);
                admin.setEmail("admin@campus.edu");
                admin.setPhone("13800000000");
                admin.setCreatedAt(LocalDateTime.now());
                admin.setUpdatedAt(LocalDateTime.now());

                userRepository.save(admin);
                System.out.println("✅ 管理员账号创建成功");
                System.out.println("   👤 用户名: admin");
                System.out.println("   🔑 密码: admin123");
                System.out.println("   📧 邮箱: admin@campus.edu");

            } else {
                User admin = userRepository.findByUsername("admin").get();
                System.out.println("✅ 管理员账号已存在: " + admin.getUsername());

                // 验证并确保密码正确
                boolean passwordValid = passwordEncoder.matches("admin123", admin.getPassword());
                if (!passwordValid) {
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    userRepository.save(admin);
                    System.out.println("🔄 管理员密码已重置为: admin123");
                } else {
                    System.out.println("✅ 管理员密码验证成功");
                }
            }

            // 显示当前用户数量
            long userCount = userRepository.count();
            System.out.println("📊 当前系统用户总数: " + userCount);

        } catch (Exception e) {
            System.out.println("❌ 管理员初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeParkingSpots() {
        try {
            // 检查是否已经初始化过
            if (parkingSpotRepository.count() > 0) {
                System.out.println("✅ 停车位数据已存在，跳过初始化");
                return;
            }

            System.out.println("🅿️ 开始初始化停车位数据...");

            // 一食堂停车区 - 30个车位
            createParkingSpots(ParkingSpot.ParkingZones.DINING_HALL, "A", 30, new BigDecimal("5.00"));
            System.out.println("   ✅ " + ParkingSpot.ParkingZones.DINING_HALL + ": 30个车位");

            // 学生活动中心 - 25个车位
            createParkingSpots(ParkingSpot.ParkingZones.STUDENT_CENTER, "B", 25, new BigDecimal("5.00"));
            System.out.println("   ✅ " + ParkingSpot.ParkingZones.STUDENT_CENTER + ": 25个车位");

            // 科大讯飞楼 - 40个车位
            createParkingSpots(ParkingSpot.ParkingZones.IFLYTEK_BUILDING, "C", 40, new BigDecimal("5.00"));
            System.out.println("   ✅ " + ParkingSpot.ParkingZones.IFLYTEK_BUILDING + ": 40个车位");

            // 南硅谷A - 35个车位
            createParkingSpots(ParkingSpot.ParkingZones.NAN_GUI_GU_A, "D", 35, new BigDecimal("5.00"));
            System.out.println("   ✅ " + ParkingSpot.ParkingZones.NAN_GUI_GU_A + ": 35个车位");

            // 创业园 - 20个车位
            createParkingSpots(ParkingSpot.ParkingZones.STARTUP_PARK, "E", 20, new BigDecimal("5.00"));
            System.out.println("   ✅ " + ParkingSpot.ParkingZones.STARTUP_PARK + ": 20个车位");

            // 演艺中心 - 15个车位
            createParkingSpots(ParkingSpot.ParkingZones.PERFORMANCE_CENTER, "F", 15, new BigDecimal("5.00"));
            System.out.println("   ✅ " + ParkingSpot.ParkingZones.PERFORMANCE_CENTER + ": 15个车位");

            // 美食街 - 25个车位
            createParkingSpots(ParkingSpot.ParkingZones.FOOD_STREET, "G", 25, new BigDecimal("5.00"));
            System.out.println("   ✅ " + ParkingSpot.ParkingZones.FOOD_STREET + ": 25个车位");

            long totalSpots = parkingSpotRepository.count();
            System.out.println("📊 停车位初始化完成，总计: " + totalSpots + "个车位");

        } catch (Exception e) {
            System.out.println("❌ 停车位初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createParkingSpots(String zone, String prefix, int count, BigDecimal rate) {
        for (int i = 1; i <= count; i++) {
            ParkingSpot spot = new ParkingSpot();
            spot.setSpotNumber(prefix + String.format("%03d", i));
            spot.setZone(zone);
            spot.setHourlyRate(rate);

            // 设置车位类型 - 每10个车位有1个残疾人车位
            if (i % 10 == 0) {
                spot.setSpotType(ParkingSpot.SpotType.DISABLED);
            } else {
                spot.setSpotType(ParkingSpot.SpotType.REGULAR);
            }

            // 设置初始状态 - 大部分空闲，少量占用和维护
            if (i % 8 == 0) {
                spot.setStatus(ParkingSpot.SpotStatus.MAINTENANCE);
            } else if (i % 5 == 0) {
                spot.setStatus(ParkingSpot.SpotStatus.OCCUPIED);
            } else {
                spot.setStatus(ParkingSpot.SpotStatus.AVAILABLE);
            }

            parkingSpotRepository.save(spot);
        }
    }
}
