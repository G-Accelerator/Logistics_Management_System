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
    const res = await authApi.login({ username, password });
    if (res.code === 200 && res.data) {
      token.value = res.data.token;
      userInfo.value = res.data.userInfo;
      localStorage.setItem("token", res.data.token);
    } else {
      throw new Error(res.message || "登录失败");
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
    const res = await authApi.getUserInfo();
    if (res.code === 200 && res.data) {
      userInfo.value = res.data;
    } else {
      throw new Error(res.message || "获取用户信息失败");
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
