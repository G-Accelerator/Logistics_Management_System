import request from "../../utils/request";
import type { LoginRequest, LoginResponse, UserInfo } from "./types";

export type { LoginRequest, LoginResponse, UserInfo };

/**
 * 用户登录
 */
export function login(data: LoginRequest): Promise<LoginResponse> {
  return request.post("/auth/login", data);
}

/**
 * 用户登出
 */
export function logout(): Promise<void> {
  return request.post("/auth/logout");
}

/**
 * 获取当前用户信息
 */
export function getUserInfo(): Promise<UserInfo> {
  return request.get("/auth/userinfo");
}
