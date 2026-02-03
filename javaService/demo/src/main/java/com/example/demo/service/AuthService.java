package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.entity.User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 认证服务
 */
@Service
public class AuthService {

    // 模拟用户数据存储
    private static final Map<String, User> USERS = new HashMap<>();
    // 模拟token存储
    private static final Map<String, User> TOKEN_STORE = new HashMap<>();
    // 买家手机号与用户映射
    private static final Map<String, User> PHONE_USERS = new HashMap<>();

    static {
        // 初始化默认用户
        USERS.put("admin", new User(1L, "admin", "123456", "管理员", "", "admin"));
        USERS.put("user", new User(2L, "user", "123456", "普通用户", "", "user"));
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
     * 买家手机号登录
     */
    public LoginResponse loginByPhone(String phone) {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式不正确");
        }

        // 为该手机号创建或获取买家用户
        User user = PHONE_USERS.computeIfAbsent(phone, p -> {
            long id = 1000L + PHONE_USERS.size();
            return new User(id, "buyer_" + p, "", "买家" + p.substring(7), "", "buyer", p);
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
