export interface MapPoint {
  /** 经度 */
  lng: number;
  /** 纬度 */
  lat: number;
  /** 站点名称/状态 */
  name: string;
  /** 详细地址 */
  address: string;
  /** 是否为起点 */
  isStart?: boolean;
  /** 是否为终点 */
  isEnd?: boolean;
}

export interface MapDialogProps {
  /** 弹窗标题 */
  title?: string;
  /** 弹窗宽度 */
  width?: string;
  /** 地图高度 */
  mapHeight?: string;
  /** 路线点位数组 */
  points: MapPoint[];
  /** 路线颜色 */
  lineColor?: string;
  /** 起点颜色 */
  startColor?: string;
  /** 终点颜色 */
  endColor?: string;
  /** 中间点颜色 */
  middleColor?: string;
}
