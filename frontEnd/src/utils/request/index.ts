import axios, { type AxiosResponse } from "axios";
import { ElMessage } from "element-plus";
import "./types"; // 导入类型扩展

// 移除对象中的空值（null, undefined, ''）
function removeEmptyParams(params: Record<string, any>): Record<string, any> {
  const result: Record<string, any> = {};
  for (const key in params) {
    const value = params[key];
    if (value !== null && value !== undefined && value !== "") {
      result[key] = value;
    }
  }
  return result;
}

const request = axios.create({
  baseURL: "http://localhost:8080/api",
  timeout: 10000,
  removeEmpty: true, // 默认开启
});

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 移除空值参数
    if (config.removeEmpty !== false && config.params) {
      config.params = removeEmptyParams(config.params);
    }
    // 添加 token 认证信息
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    // 后端统一返回 ApiResponse 格式: { code, message, data }
    // 自动解包返回 data 字段
    const result = response.data;
    if (result && typeof result === "object" && "data" in result) {
      return result.data;
    }
    return result;
  },
  (error) => {
    const message =
      error.response?.data?.message || error.message || "请求失败";
    ElMessage.error(message);
    return Promise.reject(error);
  },
);

export default request;
