import { defineStore } from "pinia";
import { ref } from "vue";
import type { UserInfo, LoginForm } from "./types";

export type { UserInfo, LoginForm };

export const useUserStore = defineStore("user", () => {
  const userInfo = ref<UserInfo | null>(null);
  const token = ref<string>("");

  // 登录
  const login = async (username: string, password: string) => {
    // 模拟登录请求
    return new Promise<void>((resolve, reject) => {
      setTimeout(() => {
        if (username && password) {
          token.value = "mock-token-" + Date.now();
          userInfo.value = {
            id: "1",
            username,
            nickname: "管理员",
            avatar: "",
            role: "admin",
          };
          localStorage.setItem("token", token.value);
          resolve();
        } else {
          reject(new Error("用户名或密码不能为空"));
        }
      }, 1000);
    });
  };

  // 登出
  const logout = () => {
    token.value = "";
    userInfo.value = null;
    localStorage.removeItem("token");
  };

  // 获取用户信息
  const getUserInfo = async () => {
    // 模拟获取用户信息
    return new Promise<void>((resolve) => {
      setTimeout(() => {
        userInfo.value = {
          id: "1",
          username: "admin",
          nickname: "管理员",
          avatar: "",
          role: "admin",
        };
        resolve();
      }, 500);
    });
  };

  return {
    userInfo,
    token,
    login,
    logout,
    getUserInfo,
  };
});
