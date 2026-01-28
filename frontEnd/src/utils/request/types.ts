// 扩展 AxiosRequestConfig 添加自定义参数
declare module "axios" {
  interface AxiosRequestConfig {
    removeEmpty?: boolean; // 是否移除空值参数
  }
}
