package com.cookie.linkpulse.controller;

import com.cookie.linkpulse.common.ApiResponse;
import com.cookie.linkpulse.dto.LoginRequest;
import com.cookie.linkpulse.dto.LoginResponse;
import com.cookie.linkpulse.entity.SysUser;
import com.cookie.linkpulse.repository.SysUserRepository;
import com.cookie.linkpulse.security.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthController(SysUserRepository sysUserRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtils jwtUtils) {
        this.sysUserRepository = sysUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        SysUser sysUser = sysUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "username or password is incorrect"));

        if (sysUser.getStatus() != 1) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), sysUser.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "username or password is incorrect");
        }

        String token = jwtUtils.generateToken(sysUser.getUsername(), sysUser.getRole());

        LoginResponse response = new LoginResponse(token, sysUser.getUsername(), sysUser.getRole());
        return ApiResponse.success(response);
    }
}