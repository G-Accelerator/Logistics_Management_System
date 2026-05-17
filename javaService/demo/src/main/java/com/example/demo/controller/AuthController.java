package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.ChangeUsernameRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.PhoneLoginRequest;
import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, e.getMessage()));
        }
    }

    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public ResponseEntity<ApiResponse<Void>> sendVerifyCode(@RequestBody PhoneLoginRequest request) {
        try {
            authService.sendVerifyCode(request.getPhone());
            return ResponseEntity.ok(ApiResponse.success("验证码已发送", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 买家/卖家手机号登录
     */
    @PostMapping("/login/phone")
    public ResponseEntity<ApiResponse<LoginResponse>> loginByPhone(@RequestBody PhoneLoginRequest request) {
        try {
            String role = request.getRole() != null ? request.getRole() : "buyer";
            LoginResponse response = authService.loginByPhone(request.getPhone(), request.getCode(), role);
            return ResponseEntity.ok(ApiResponse.success("验证成功", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, e.getMessage()));
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            authService.logout(token);
        }
        return ResponseEntity.ok(ApiResponse.success("登出成功", null));
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/userinfo")
    public ResponseEntity<ApiResponse<LoginResponse.UserInfo>> getUserInfo(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(401, "未登录"));
            }
            String token = authorization.substring(7);
            LoginResponse.UserInfo userInfo = authService.getUserInfo(token);
            return ResponseEntity.ok(ApiResponse.success(userInfo));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, e.getMessage()));
        }
    }

    /**
     * 更新昵称（管理员）
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<LoginResponse.UserInfo>> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody UpdateProfileRequest request) {
        try {
            String token = requireToken(authorization);
            LoginResponse.UserInfo info = authService.updateProfile(token, request);
            return ResponseEntity.ok(ApiResponse.success("保存成功", info));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 修改登录用户名（管理员）
     */
    @PutMapping("/username")
    public ResponseEntity<ApiResponse<LoginResponse.UserInfo>> changeUsername(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ChangeUsernameRequest request) {
        try {
            String token = requireToken(authorization);
            LoginResponse.UserInfo info = authService.changeUsername(token, request);
            return ResponseEntity.ok(ApiResponse.success("用户名已更新", info));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 修改密码（管理员）
     */
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ChangePasswordRequest request) {
        try {
            String token = requireToken(authorization);
            authService.changePassword(token, request);
            return ResponseEntity.ok(ApiResponse.success("密码已修改", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    private String requireToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("未登录");
        }
        return authorization.substring(7);
    }

    /**
     * 根据token获取用户手机号（内部使用）
     */
    public String getPhoneByToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring(7);
        try {
            LoginResponse.UserInfo userInfo = authService.getUserInfo(token);
            return userInfo.getPhone();
        } catch (Exception e) {
            return null;
        }
    }
}
