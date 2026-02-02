export interface TrackInfo {
  trackingNo: string;
  expressCompanyName?: string;
  origin: string;
  destination: string;
  status: string;
  statusText: string;
  sendTime: string;
  estimatedTime: string;
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
