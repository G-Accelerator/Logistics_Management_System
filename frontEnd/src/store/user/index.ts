import { defineStore } from "pinia";
import { ref } from "vue";
import type { UserInfo, LoginForm } from "./types";
import * as authApi from "../../api/auth";

export type { UserInfo, LoginForm };

export const useUserStore = defineStore("user", () => {
  const userInfo = ref<UserInfo | null>(null);
  const token = ref<string>(localStorage.getItem("token") || "");
  const buyerPhone = ref<string>(localStorage.getItem("buyerPhone") || "");

  // 账号密码登录
  const login = async (username: string, password: string) => {
    const data: authApi.LoginResponse = await authApi.login({
      username,
      password,
    });
    if (data && data.token) {
      token.value = data.token;
      userInfo.value = data.userInfo;
      buyerPhone.value = "";
      localStorage.setItem("token", data.token);
      localStorage.removeItem("buyerPhone");
    } else {
      throw new Error("登录失败");
    }
  };

  // 买家手机号登录
  const loginByPhone = async (phone: string) => {
    const data: authApi.LoginResponse = await authApi.loginByPhone(phone);
    if (data && data.token) {
      token.value = data.token;
      userInfo.value = data.userInfo;
      buyerPhone.value = phone;
      localStorage.setItem("token", data.token);
      localStorage.setItem("buyerPhone", phone);
    } else {
      throw new Error("验证失败");
    }
  };

  // 登出
  const logout = async () => {
    try {
      await authApi.logout();
    } finally {
      token.value = "";
      userInfo.value = null;
      buyerPhone.value = "";
      localStorage.removeItem("token");
      localStorage.removeItem("buyerPhone");
    }
  };

  // 获取用户信息
  const getUserInfo = async () => {
    const data: authApi.UserInfo = await authApi.getUserInfo();
    if (data) {
      userInfo.value = data;
    } else {
      throw new Error("获取用户信息失败");
    }
  };

  // 是否为买家角色
  const isBuyer = () => userInfo.value?.role === "buyer";

  return {
    userInfo,
    token,
    buyerPhone,
    login,
    loginByPhone,
    logout,
    getUserInfo,
    isBuyer,
  };
});
