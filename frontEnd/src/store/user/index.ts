import { defineStore } from "pinia";
import { ref } from "vue";
import type { UserInfo, LoginForm } from "./types";
import * as authApi from "../../api/auth";

export type { UserInfo, LoginForm };

export const useUserStore = defineStore("user", () => {
  const userInfo = ref<UserInfo | null>(null);
  const token = ref<string>(localStorage.getItem("token") || "");

  // 登录
  const login = async (username: string, password: string) => {
    // 响应拦截器已自动解包 ApiResponse，直接拿到 LoginResponse
    const data: authApi.LoginResponse = await authApi.login({
      username,
      password,
    });
    if (data && data.token) {
      token.value = data.token;
      userInfo.value = data.userInfo;
      localStorage.setItem("token", data.token);
    } else {
      throw new Error("登录失败");
    }
  };

  // 登出
  const logout = async () => {
    try {
      await authApi.logout();
    } finally {
      token.value = "";
      userInfo.value = null;
      localStorage.removeItem("token");
    }
  };

  // 获取用户信息
  const getUserInfo = async () => {
    // 响应拦截器已自动解包 ApiResponse，直接拿到 UserInfo
    const data: authApi.UserInfo = await authApi.getUserInfo();
    if (data) {
      userInfo.value = data;
    } else {
      throw new Error("获取用户信息失败");
    }
  };

  return {
    userInfo,
    token,
    login,
    logout,
    getUserInfo,
  };
});
