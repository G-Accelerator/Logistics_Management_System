package com.example.demo.service;

/**
 * 登录会话中的用户主体（管理员来自数据库，买家/卖家来自手机号登录）
 */
public class AuthPrincipal {

    private final Long id;
    private final String username;
    private String nickname;
    private final String avatar;
    private final String role;
    private final String phone;
    /** 管理员账号，可修改资料与密码 */
    private final boolean adminAccount;

    public AuthPrincipal(
            Long id,
            String username,
            String nickname,
            String avatar,
            String role,
            String phone,
            boolean adminAccount) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.avatar = avatar != null ? avatar : "";
        this.role = role;
        this.phone = phone;
        this.adminAccount = adminAccount;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getRole() {
        return role;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isAdminAccount() {
        return adminAccount;
    }
}
