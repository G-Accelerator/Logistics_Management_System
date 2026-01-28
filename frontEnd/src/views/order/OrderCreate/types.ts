export interface TrackPoint {
  time: string;
  status: string;
  location: string;
  lng: number;
  lat: number;
  passed: boolean;
  isCurrent?: boolean;
}

export interface RouteOption {
  label: string;
  tagType: "primary" | "success" | "warning" | "info";
  description: string;
  distance: number;
  duration: number;
  pathPoints: [number, number][];
  policy: number;
  trackPoints?: TrackPoint[];
}
