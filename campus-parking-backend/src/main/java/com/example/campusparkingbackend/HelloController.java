package com.example.campusparkingbackend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api") // 所有这个Controller的接口都以/api开头
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Spring Boot Backend! 校园停车管理系统后端服务已启动！";
    }

    @GetMapping("/test")
    public String test() {
        return "测试接口成功！后端服务运行正常！";
    }
}
