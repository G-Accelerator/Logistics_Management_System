import request from "../../utils/request";

/** 与后端 VehicleService 固定选项一致 */
export const VEHICLE_TYPE_OPTIONS = ["大货车", "中货车", "小货车"] as const;

export type VehicleType = (typeof VEHICLE_TYPE_OPTIONS)[number];

export interface Vehicle {
  id?: number;
  plateNumber: string;
  vehicleType: VehicleType | string;
  driverName: string;
  driverPhone: string;
  speedLimitKmh: number;
  enabled: boolean;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface PageResult<T> {
  data: T[];
  total: number;
}

export function getVehicles(
  page: number,
  pageSize: number,
): Promise<PageResult<Vehicle>> {
  return request.get("/vehicles", { params: { page, pageSize } });
}

export function createVehicle(data: Vehicle) {
  return request.post("/vehicles", data);
}

export function updateVehicle(id: number, data: Vehicle) {
  return request.put(`/vehicles/${id}`, data);
}

export function deleteVehicle(id: number) {
  return request.delete(`/vehicles/${id}`);
}

/** 发货可选：启用且未被运输中单占用 */
export function getVehiclesAvailableForShip(): Promise<Vehicle[]> {
  return request.get("/vehicles/available-for-ship");
}

export interface VehicleMonitoringRow {
  vehicleId: number;
  plateNumber: string;
  vehicleType: string;
  driverName: string;
  driverPhone: string;
  speedLimitKmh: number;
  online: boolean;
  orderNo: string;
  trackingNo: string;
  currentSpeedKmh: number;
  overspeed: boolean;
  offlineAlert: boolean;
}

export function getVehicleMonitoring(): Promise<VehicleMonitoringRow[]> {
  return request.get("/vehicles/monitoring");
}

export type VehicleNotifyTemplateType = "OVERSPEED" | "OFFLINE" | "REGULAR";

export function previewVehicleNotify(
  vehicleId: number,
  orderNo: string,
  templateType: VehicleNotifyTemplateType,
): Promise<string> {
  return request.get("/vehicles/notify/preview", {
    params: { vehicleId, orderNo, templateType },
  });
}

export function sendVehicleNotify(payload: {
  vehicleId: number;
  orderNo: string;
  templateType: VehicleNotifyTemplateType;
  content: string;
}): Promise<string> {
  return request.post("/vehicles/notify", payload);
}
