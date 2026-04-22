package com.cookie.linkpulse.config;

import com.cookie.linkpulse.entity.SysUser;
import com.cookie.linkpulse.repository.SysUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initAdminUser(SysUserRepository sysUserRepository,
                                           PasswordEncoder passwordEncoder) {
        return args -> {
            if (!sysUserRepository.existsByUsername("admin")) {
                SysUser admin = new SysUser();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("Admin@123456"));
                admin.setRole("ADMIN");
                admin.setStatus(1);
                admin.setCreatedAt(LocalDateTime.now());
                admin.setUpdatedAt(LocalDateTime.now());

                sysUserRepository.save(admin);
                System.out.println("Default admin user initialized: admin / Admin@123456");
            }
        };
    }
}