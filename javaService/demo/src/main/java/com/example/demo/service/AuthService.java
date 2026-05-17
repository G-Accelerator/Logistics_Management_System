package com.example.demo.service;

import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.ChangeUsernameRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.entity.UserAccount;
import com.example.demo.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证服务
 */
@Service
public class AuthService {

    private static final Logger VERIFY_LOG =
        LoggerFactory.getLogger("com.example.demo.auth.verifycode");

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    private final Map<String, AuthPrincipal> tokenStore = new ConcurrentHashMap<>();
    private final Map<String, AuthPrincipal> phoneUsers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, VerifyCodeInfo> verifyCodes = new ConcurrentHashMap<>();

    /** 验证码有效期：2 分钟 */
    private static final long CODE_EXPIRE_TIME = 2 * 60 * 1000L;

    public AuthService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private static class VerifyCodeInfo {
        String code;
        long createTime;

        VerifyCodeInfo(String code) {
            this.code = code;
            this.createTime = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createTime > CODE_EXPIRE_TIME;
        }
    }

    public void sendVerifyCode(String phone) {
        phone = normalizePhone(phone);
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式不正确");
        }
        String code = String.format("%06d", new Random().nextInt(1000000));
        verifyCodes.put(phone, new VerifyCodeInfo(code));
        VERIFY_LOG.info("[登录验证码] 手机号 {} 验证码 {}（2分钟内有效）", phone, code);
    }

    private static String normalizePhone(String phone) {
        return phone != null ? phone.trim() : null;
    }

    private static String normalizeCode(String code) {
        return code != null ? code.trim() : "";
    }

    private boolean verifyCode(String phone, String code) {
        phone = normalizePhone(phone);
        code = normalizeCode(code);
        VerifyCodeInfo info = verifyCodes.get(phone);
        if (info != null && !info.isExpired() && info.code.equals(code)) {
            verifyCodes.remove(phone);
            return true;
        }
        if (info != null && info.isExpired()) {
            verifyCodes.remove(phone);
        }
        return false;
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        String password = request.getPassword();

        if (username.isEmpty() || password == null || password.isEmpty()) {
            throw new RuntimeException("用户名或密码错误");
        }

        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!"admin".equals(account.getRole())) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (Boolean.FALSE.equals(account.getEnabled())) {
            throw new RuntimeException("账号已禁用");
        }
        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }

        AuthPrincipal principal = toPrincipal(account);
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, principal);

        return new LoginResponse(token, toUserInfo(principal));
    }

    public LoginResponse loginByPhone(String phone, String code, String role) {
        phone = normalizePhone(phone);
        code = normalizeCode(code);
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式不正确");
        }
        if (!verifyCode(phone, code)) {
            throw new RuntimeException("验证码错误或已过期");
        }

        String userRole = "seller".equals(role) ? "seller" : "buyer";
        String nickname = "seller".equals(role) ? "卖家" : "买家";
        String userKey = userRole + "_" + phone;

        AuthPrincipal principal = phoneUsers.computeIfAbsent(userKey, p -> {
            long id = 1000L + phoneUsers.size();
            return new AuthPrincipal(
                    id,
                    userKey,
                    nickname + phone.substring(7),
                    "",
                    userRole,
                    phone,
                    false);
        });

        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, principal);

        return new LoginResponse(token, toUserInfo(principal));
    }

    public void logout(String token) {
        tokenStore.remove(token);
    }

    public LoginResponse.UserInfo getUserInfo(String token) {
        AuthPrincipal principal = requirePrincipal(token);
        return toUserInfo(principal);
    }

    @Transactional
    public LoginResponse.UserInfo updateProfile(String token, UpdateProfileRequest request) {
        AuthPrincipal principal = requireAdminPrincipal(token);
        UserAccount account = userAccountRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("账号不存在"));

        String nickname = request.getNickname() != null ? request.getNickname().trim() : "";
        if (nickname.isEmpty()) {
            throw new RuntimeException("昵称不能为空");
        }
        if (nickname.length() > 64) {
            throw new RuntimeException("昵称过长");
        }

        account.setNickname(nickname);
        userAccountRepository.save(account);

        principal.setNickname(nickname);
        syncTokenPrincipal(token, principal);
        return toUserInfo(principal);
    }

    @Transactional
    public LoginResponse.UserInfo changeUsername(String token, ChangeUsernameRequest request) {
        AuthPrincipal principal = requireAdminPrincipal(token);
        UserAccount account = userAccountRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("账号不存在"));

        String newUsername = request.getNewUsername() != null ? request.getNewUsername().trim() : "";
        String password = request.getPassword();

        if (newUsername.isEmpty()) {
            throw new RuntimeException("新用户名不能为空");
        }
        if (newUsername.length() < 3 || newUsername.length() > 32) {
            throw new RuntimeException("用户名长度应为 3–32 个字符");
        }
        if (!newUsername.matches("^[a-zA-Z0-9_]+$")) {
            throw new RuntimeException("用户名仅支持字母、数字和下划线");
        }
        if (password == null || password.isEmpty()) {
            throw new RuntimeException("请输入当前密码以确认身份");
        }
        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new RuntimeException("当前密码错误");
        }
        if (userAccountRepository.existsByUsernameAndIdNot(newUsername, account.getId())) {
            throw new RuntimeException("用户名已被占用");
        }

        account.setUsername(newUsername);
        userAccountRepository.save(account);

        AuthPrincipal updated = new AuthPrincipal(
                account.getId(),
                newUsername,
                account.getNickname(),
                account.getAvatar(),
                account.getRole(),
                account.getPhone(),
                true);
        tokenStore.put(token, updated);
        return toUserInfo(updated);
    }

    @Transactional
    public void changePassword(String token, ChangePasswordRequest request) {
        AuthPrincipal principal = requireAdminPrincipal(token);
        UserAccount account = userAccountRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("账号不存在"));

        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();

        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new RuntimeException("请输入原密码");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("新密码至少 6 位");
        }
        if (!passwordEncoder.matches(oldPassword, account.getPasswordHash())) {
            throw new RuntimeException("原密码错误");
        }
        if (passwordEncoder.matches(newPassword, account.getPasswordHash())) {
            throw new RuntimeException("新密码不能与原密码相同");
        }

        account.setPasswordHash(passwordEncoder.encode(newPassword));
        userAccountRepository.save(account);
    }

    private AuthPrincipal requirePrincipal(String token) {
        AuthPrincipal principal = tokenStore.get(token);
        if (principal == null) {
            throw new RuntimeException("token无效或已过期");
        }
        return principal;
    }

    private AuthPrincipal requireAdminPrincipal(String token) {
        AuthPrincipal principal = requirePrincipal(token);
        if (!principal.isAdminAccount()) {
            throw new RuntimeException("仅管理员账号可修改");
        }
        return principal;
    }

    private void syncTokenPrincipal(String token, AuthPrincipal principal) {
        tokenStore.put(token, principal);
    }

    private AuthPrincipal toPrincipal(UserAccount account) {
        return new AuthPrincipal(
                account.getId(),
                account.getUsername(),
                account.getNickname(),
                account.getAvatar(),
                account.getRole(),
                account.getPhone(),
                true);
    }

    private LoginResponse.UserInfo toUserInfo(AuthPrincipal principal) {
        return new LoginResponse.UserInfo(
                principal.getId().toString(),
                principal.getUsername(),
                principal.getNickname(),
                principal.getAvatar(),
                principal.getRole(),
                principal.getPhone());
    }
}
