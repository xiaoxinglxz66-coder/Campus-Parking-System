package com.example.campusparkingbackend.service;

import com.example.campusparkingbackend.entity.User;
import com.example.campusparkingbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("=== 开始用户认证 ===");
        System.out.println("用户名: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("用户不存在: " + username);
                    return new UsernameNotFoundException("用户不存在: " + username);
                });

        System.out.println("找到用户: " + user.getUsername());
        System.out.println("密码哈希: " + user.getPassword());
        System.out.println("用户类型: " + user.getUserType().name());

        // 统一添加 ROLE_ 前缀，与注册时保持一致
        String role = "ROLE_" + user.getUserType().name();
        System.out.println("分配角色: " + role);

        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(role)
        );

        System.out.println("=== 用户认证完成 ===");

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
