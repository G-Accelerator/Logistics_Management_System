// 登录请求参数
export interface LoginRequest {
  username: string;
  password: string;
}

// 用户信息
export interface UserInfo {
  id: string;
  username: string;
  nickname: string;
  avatar: string;
  role: string;
  phone?: string;
}

// 登录响应
export interface LoginResponse {
  token: string;
  userInfo: UserInfo;
}

// API响应格式
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}
