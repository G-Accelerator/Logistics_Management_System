// 高德地图类型声明
declare const AMap: {
  Map: new (container: string | HTMLElement, options?: any) => any;
  Marker: new (options?: any) => any;
  Polyline: new (options?: any) => any;
  InfoWindow: new (options?: any) => any;
  Icon: new (options?: any) => any;
  Size: new (width: number, height: number) => any;
  Pixel: new (x: number, y: number) => any;
  Scale: new (options?: any) => any;
  ToolBar: new (options?: any) => any;
  Bounds: new (southWest: [number, number], northEast: [number, number]) => any;
  plugin: (plugins: string | string[], callback: () => void) => void;
  Driving: new (options?: any) => any;
  Geocoder: new (options?: any) => any;
  DrivingPolicy: {
    LEAST_TIME: number;
    LEAST_FEE: number;
    LEAST_DISTANCE: number;
    REAL_TRAFFIC: number;
  };
};
