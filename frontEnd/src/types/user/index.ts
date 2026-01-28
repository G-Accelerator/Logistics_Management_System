// 用户相关类型定义
export interface LoginForm {
  username: string;
  password: string;
}

export interface UserInfo {
  id: string;
  username: string;
  nickname: string;
  avatar?: string;
  role: string;
}
