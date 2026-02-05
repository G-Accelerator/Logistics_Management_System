/**
 * 文件下载工具函数
 * 处理 Blob 响应并触发浏览器下载
 */

/**
 * 下载 Blob 数据为文件
 * @param blob - Blob 数据
 * @param filename - 文件名（可选，如果不提供则尝试从 Content-Disposition 获取）
 */
export function downloadBlob(blob: Blob, filename?: string): void {
  // 创建临时 URL
  const url = window.URL.createObjectURL(blob);

  // 创建隐藏的 <a> 元素
  const link = document.createElement("a");
  link.href = url;
  link.style.display = "none";

  // 设置文件名
  if (filename) {
    link.download = filename;
  }

  // 添加到 DOM 并触发点击
  document.body.appendChild(link);
  link.click();

  // 清理：移除元素并释放 URL
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}

/**
 * 生成带时间戳的文件名
 * @param prefix - 文件名前缀
 * @param extension - 文件扩展名（不含点号）
 * @returns 格式如 "订单数据_20260205.xlsx"
 */
export function generateTimestampFilename(
  prefix: string,
  extension: string,
): string {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  const timestamp = `${year}${month}${day}`;

  return `${prefix}_${timestamp}.${extension}`;
}

/**
 * 下载订单导出文件
 * 封装了文件名生成逻辑，符合 Requirements 1.5
 * @param blob - Excel Blob 数据
 */
export function downloadOrderExport(blob: Blob): void {
  const filename = generateTimestampFilename("订单数据", "xlsx");
  downloadBlob(blob, filename);
}

/**
 * 下载导入模板
 * @param blob - Excel Blob 数据
 */
export function downloadImportTemplate(blob: Blob): void {
  downloadBlob(blob, "订单导入模板.xlsx");
}

/**
 * 下载导入失败记录
 * @param blob - Excel Blob 数据
 */
export function downloadImportErrors(blob: Blob): void {
  const filename = generateTimestampFilename("导入失败记录", "xlsx");
  downloadBlob(blob, filename);
}
