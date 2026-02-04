import { defineStore } from "pinia";
import { ref } from "vue";
import type { UserInfo, LoginForm } from "./types";
import * as authApi from "../../api/auth";

export type { UserInfo, LoginForm };

export const useUserStore = defineStore("user", () => {
  const userInfo = ref<UserInfo | null>(null);
  const token = ref<string>(localStorage.getItem("token") || "");
  const buyerPhone = ref<string>(localStorage.getItem("buyerPhone") || "");
  const sellerPhone = ref<string>(localStorage.getItem("sellerPhone") || "");

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
      sellerPhone.value = "";
      localStorage.setItem("token", data.token);
      localStorage.removeItem("buyerPhone");
      localStorage.removeItem("sellerPhone");
    } else {
      throw new Error("登录失败");
    }
  };

  // 手机号登录（买家/卖家）
  const loginByPhone = async (
    phone: string,
    code: string,
    role: "buyer" | "seller",
  ) => {
    const data: authApi.LoginResponse = await authApi.loginByPhone(
      phone,
      code,
      role,
    );
    if (data && data.token) {
      token.value = data.token;
      userInfo.value = data.userInfo;
      if (role === "buyer") {
        buyerPhone.value = phone;
        sellerPhone.value = "";
        localStorage.setItem("buyerPhone", phone);
        localStorage.removeItem("sellerPhone");
      } else {
        sellerPhone.value = phone;
        buyerPhone.value = "";
        localStorage.setItem("sellerPhone", phone);
        localStorage.removeItem("buyerPhone");
      }
      localStorage.setItem("token", data.token);
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
      sellerPhone.value = "";
      localStorage.removeItem("token");
      localStorage.removeItem("buyerPhone");
      localStorage.removeItem("sellerPhone");
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

  // 是否为卖家角色
  const isSeller = () => userInfo.value?.role === "seller";

  return {
    userInfo,
    token,
    buyerPhone,
    sellerPhone,
    login,
    loginByPhone,
    logout,
    getUserInfo,
    isBuyer,
    isSeller,
  };
});
