package com.example.demo.dto;

/**
 * 手机号登录请求
 */
public class PhoneLoginRequest {
    private String phone;

    public PhoneLoginRequest() {}

    public PhoneLoginRequest(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
