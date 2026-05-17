/** 经纬度点 [lng, lat] */
export type LngLatPair = [number, number];

/** 二次贝塞尔曲线，站点间弯道展示（不调用路网 API） */
export function bezierSegmentPath(
  start: LngLatPair,
  end: LngLatPair,
  segmentIndex = 0,
  pointCount = 24,
): LngLatPair[] {
  const [lng1, lat1] = start;
  const [lng2, lat2] = end;
  const midLng = (lng1 + lng2) / 2;
  const midLat = (lat1 + lat2) / 2;
  const dx = lng2 - lng1;
  const dy = lat2 - lat1;
  const dist = Math.hypot(dx, dy) || 1e-6;
  const bow = Math.min(dist * 0.2, 1.5) * (segmentIndex % 2 === 0 ? 1 : -1);
  const ctrlLng = midLng - (dy / dist) * bow;
  const ctrlLat = midLat + (dx / dist) * bow;

  const path: LngLatPair[] = [];
  for (let i = 0; i <= pointCount; i++) {
    const t = i / pointCount;
    const u = 1 - t;
    path.push([
      u * u * lng1 + 2 * u * t * ctrlLng + t * t * lng2,
      u * u * lat1 + 2 * u * t * ctrlLat + t * t * lat2,
    ]);
  }
  return path;
}

function resolveSegmentPath(
  start: LngLatPair,
  end: LngLatPair,
  segmentIndex: number,
): LngLatPair[] {
  if (start[0] === end[0] && start[1] === end[1]) {
    return [start];
  }
  return bezierSegmentPath(start, end, segmentIndex);
}

/** 串联多站点为一条连续弯道轨迹 */
export function buildRouteThroughWaypoints(waypoints: LngLatPair[]): LngLatPair[] {
  if (waypoints.length === 0) return [];
  if (waypoints.length === 1) return [waypoints[0]!];

  let merged: LngLatPair[] = [];
  for (let i = 0; i < waypoints.length - 1; i++) {
    const seg = resolveSegmentPath(waypoints[i]!, waypoints[i + 1]!, i);
    if (i === 0) merged = seg;
    else merged = merged.concat(seg.slice(1));
  }
  return merged;
}

export function coordsFromTrackPoints(
  points: { lng: number; lat: number }[],
): LngLatPair[] {
  return points.map((p) => [p.lng, p.lat]);
}
