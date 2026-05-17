export interface TrackInfo {
  trackingNo: string;
  expressCompanyName?: string;
  origin: string;
  destination: string;
  status: string;
  statusText: string;
  sendTime: string;
  estimatedTime: string;
  /** 承运车辆（运输中且已绑车时） */
  vehiclePlateNumber?: string;
  vehicleType?: string;
  vehicleDriverName?: string;
  vehicleDriverPhone?: string;
  vehicleOnline?: boolean;
  currentSpeedKmh?: number;
}

export interface TrackPoint {
  time: string;
  status: string;
  location: string;
  lng: number;
  lat: number;
  passed: boolean;
  isCurrent?: boolean;
  estimatedTime?: string;
}
