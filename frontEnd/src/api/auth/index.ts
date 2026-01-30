import request from "../../utils/request";
import type {
  LoginRequest,
  LoginResponse,
  UserInfo,
  ApiResponse,
} from "./types";

export type { LoginRequest, LoginResponse, UserInfo };

/**
 * 用户登录
 */
export function login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
  return request.post("/auth/login", data);
}

/**
 * 用户登出
 */
export function logout(): Promise<ApiResponse<void>> {
  return request.post("/auth/logout");
}

/**
 * 获取当前用户信息
 */
export function getUserInfo(): Promise<ApiResponse<UserInfo>> {
  return request.get("/auth/userinfo");
}
