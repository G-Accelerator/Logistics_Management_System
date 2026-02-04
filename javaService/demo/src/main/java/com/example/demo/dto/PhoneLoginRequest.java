package com.example.demo.dto;

/**
 * 手机号登录请求
 */
public class PhoneLoginRequest {
    private String phone;
    private String code;
    private String role; // buyer 或 seller

    public PhoneLoginRequest() {}

    public PhoneLoginRequest(String phone, String code, String role) {
        this.phone = phone;
        this.code = code;
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
