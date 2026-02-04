package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证服务
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    // 模拟用户数据存储
    private static final Map<String, User> USERS = new HashMap<>();
    // 模拟token存储
    private static final Map<String, User> TOKEN_STORE = new HashMap<>();
    // 买家手机号与用户映射
    private static final Map<String, User> PHONE_USERS = new HashMap<>();
    // 验证码存储 (手机号 -> 验证码)
    private static final ConcurrentHashMap<String, String> VERIFY_CODES = new ConcurrentHashMap<>();

    static {
        // 初始化默认用户
        USERS.put("admin", new User(1L, "admin", "123456", "管理员", "", "admin"));
        USERS.put("user", new User(2L, "user", "123456", "普通用户", "", "user"));
    }

    /**
     * 发送验证码
     */
    public void sendVerifyCode(String phone) {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式不正确");
        }
        
        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        VERIFY_CODES.put(phone, code);
        
        // 打印验证码到控制台
        log.info("========================================");
        log.info("手机号 {} 的验证码: {}", phone, code);
        log.info("========================================");
    }

    /**
     * 验证验证码
     */
    private boolean verifyCode(String phone, String code) {
        String storedCode = VERIFY_CODES.get(phone);
        if (storedCode != null && storedCode.equals(code)) {
            VERIFY_CODES.remove(phone); // 验证成功后删除
            return true;
        }
        return false;
    }

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        // 验证用户名和密码
        User user = USERS.get(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成token
        String token = UUID.randomUUID().toString().replace("-", "");
        TOKEN_STORE.put(token, user);

        // 构建响应
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
            user.getId().toString(),
            user.getUsername(),
            user.getNickname(),
            user.getAvatar(),
            user.getRole()
        );

        return new LoginResponse(token, userInfo);
    }

    /**
     * 买家/卖家手机号登录
     */
    public LoginResponse loginByPhone(String phone, String code, String role) {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式不正确");
        }

        // 验证验证码
        if (!verifyCode(phone, code)) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 根据角色创建用户
        String userRole = "seller".equals(role) ? "seller" : "buyer";
        String nickname = "seller".equals(role) ? "卖家" : "买家";
        
        // 为该手机号创建或获取用户
        String userKey = userRole + "_" + phone;
        User user = PHONE_USERS.computeIfAbsent(userKey, p -> {
            long id = 1000L + PHONE_USERS.size();
            return new User(id, userKey, "", nickname + phone.substring(7), "", userRole, phone);
        });

        // 生成token
        String token = UUID.randomUUID().toString().replace("-", "");
        TOKEN_STORE.put(token, user);

        // 构建响应
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
            user.getId().toString(),
            user.getUsername(),
            user.getNickname(),
            user.getAvatar(),
            user.getRole(),
            phone
        );

        return new LoginResponse(token, userInfo);
    }

    /**
     * 用户登出
     */
    public void logout(String token) {
        TOKEN_STORE.remove(token);
    }

    /**
     * 根据token获取用户信息
     */
    public LoginResponse.UserInfo getUserInfo(String token) {
        User user = TOKEN_STORE.get(token);
        if (user == null) {
            throw new RuntimeException("token无效或已过期");
        }

        return new LoginResponse.UserInfo(
            user.getId().toString(),
            user.getUsername(),
            user.getNickname(),
            user.getAvatar(),
            user.getRole(),
            user.getPhone()
        );
    }
}
