package com.example.demo.dto;

public class ChangeUsernameRequest {
    private String newUsername;
    /** 当前密码，用于校验身份 */
    private String password;

    public String getNewUsername() {
        return newUsername;
    }

    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
