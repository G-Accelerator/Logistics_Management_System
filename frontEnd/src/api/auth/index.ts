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
 * 发送验证码
 */
export function sendVerifyCode(phone: string): Promise<void> {
  return request.post("/auth/send-code", { phone });
}

/**
 * 买家/卖家手机号登录
 */
export function loginByPhone(
  phone: string,
  code: string,
  role: "buyer" | "seller" = "buyer",
): Promise<LoginResponse> {
  return request.post("/auth/login/phone", { phone, code, role });
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
