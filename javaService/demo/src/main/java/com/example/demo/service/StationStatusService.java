package com.example.demo.service;

import com.example.demo.dto.StationInfo;
import com.example.demo.dto.RoutePlanResponse.TrackPoint;
import com.example.demo.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 站点状态服务
 * 负责管理物流运输过程中每个站点的到达状态
 */
@Service
public class StationStatusService {

    private static final Logger log = LoggerFactory.getLogger(StationStatusService.class);
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_ARRIVED = "arrived";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OrderService orderService;

    public StationStatusService(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 获取订单站点状态列表
     * @param orderNo 订单号
     * @return 站点信息列表
     */
    public List<StationInfo> getStationStatus(String orderNo) {
        Order order = orderService.getOrder(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }

        List<TrackPoint> trackPoints = order.getTrackPoints();
        if (trackPoints == null || trackPoints.isEmpty()) {
            throw new IllegalArgumentException("该订单无站点数据");
        }

        List<StationInfo> stations = new ArrayList<>();
        for (int i = 0; i < trackPoints.size(); i++) {
            TrackPoint tp = trackPoints.get(i);
            StationInfo info = new StationInfo(
                i,
                tp.getLocation(),
                tp.getLng(),
                tp.getLat(),
                tp.getArrivalStatus() != null ? tp.getArrivalStatus() : STATUS_PENDING,
                tp.getArrivalTime()
            );
            stations.add(info);
        }
        return stations;
    }


    /**
     * 标记单个站点到达
     * @param orderNo 订单号
     * @param stationIndex 站点索引
     * @return 更新后的站点信息
     */
    public StationInfo markStationArrived(String orderNo, int stationIndex) {
        Order order = orderService.getOrder(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }

        List<TrackPoint> trackPoints = order.getTrackPoints();
        if (trackPoints == null || trackPoints.isEmpty()) {
            throw new IllegalArgumentException("该订单无站点数据");
        }

        if (stationIndex < 0 || stationIndex >= trackPoints.size()) {
            throw new IllegalArgumentException("站点索引无效");
        }

        TrackPoint trackPoint = trackPoints.get(stationIndex);
        
        // 检查站点是否已到达
        if (STATUS_ARRIVED.equals(trackPoint.getArrivalStatus())) {
            throw new IllegalArgumentException("该站点已标记到达");
        }

        // 顺序验证
        if (!canMarkStation(order, stationIndex)) {
            throw new IllegalArgumentException("请先标记前一个站点到达");
        }

        // 标记到达
        String arrivalTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        trackPoint.setArrivalStatus(STATUS_ARRIVED);
        trackPoint.setArrivalTime(arrivalTime);

        // 持久化
        orderService.updateOrder(order);
        log.info("订单 {} 站点 {} 已标记到达", orderNo, stationIndex);

        return new StationInfo(
            stationIndex,
            trackPoint.getLocation(),
            trackPoint.getLng(),
            trackPoint.getLat(),
            STATUS_ARRIVED,
            arrivalTime
        );
    }

    /**
     * 标记全部站点到达
     * @param orderNo 订单号
     * @return 标记到达的站点数量
     */
    public int markAllStationsArrived(String orderNo) {
        Order order = orderService.getOrder(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }

        List<TrackPoint> trackPoints = order.getTrackPoints();
        if (trackPoints == null || trackPoints.isEmpty()) {
            throw new IllegalArgumentException("该订单无站点数据");
        }

        int arrivedCount = 0;
        String arrivalTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        for (TrackPoint tp : trackPoints) {
            if (!STATUS_ARRIVED.equals(tp.getArrivalStatus())) {
                tp.setArrivalStatus(STATUS_ARRIVED);
                tp.setArrivalTime(arrivalTime);
                arrivedCount++;
            }
        }

        if (arrivedCount > 0) {
            orderService.updateOrder(order);
            log.info("订单 {} 全部 {} 个站点已标记到达", orderNo, arrivedCount);
        }

        return arrivedCount;
    }


    /**
     * 标记到达至指定站点
     * @param orderNo 订单号
     * @param targetIndex 目标站点索引
     * @return 标记到达的站点数量
     */
    public int markStationsArrivedTo(String orderNo, int targetIndex) {
        Order order = orderService.getOrder(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }

        List<TrackPoint> trackPoints = order.getTrackPoints();
        if (trackPoints == null || trackPoints.isEmpty()) {
            throw new IllegalArgumentException("该订单无站点数据");
        }

        if (targetIndex < 0 || targetIndex >= trackPoints.size()) {
            throw new IllegalArgumentException("站点索引无效");
        }

        int arrivedCount = 0;
        String arrivalTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        // 从起点到目标站点，按顺序标记所有未到达的站点
        for (int i = 0; i <= targetIndex; i++) {
            TrackPoint tp = trackPoints.get(i);
            if (!STATUS_ARRIVED.equals(tp.getArrivalStatus())) {
                tp.setArrivalStatus(STATUS_ARRIVED);
                tp.setArrivalTime(arrivalTime);
                arrivedCount++;
            }
        }

        if (arrivedCount > 0) {
            orderService.updateOrder(order);
            log.info("订单 {} 已标记到达至站点 {}，共 {} 个站点", orderNo, targetIndex, arrivedCount);
        }

        return arrivedCount;
    }

    /**
     * 验证是否可以标记该站点（顺序验证）
     * 只有当前一个站点已到达时，才能标记下一个站点到达
     * @param order 订单
     * @param stationIndex 站点索引
     * @return 是否可以标记
     */
    public boolean canMarkStation(Order order, int stationIndex) {
        if (stationIndex == 0) {
            // 第一个站点（起点）可以直接标记
            return true;
        }

        List<TrackPoint> trackPoints = order.getTrackPoints();
        if (trackPoints == null || stationIndex >= trackPoints.size()) {
            return false;
        }

        // 检查前一个站点是否已到达
        TrackPoint previousPoint = trackPoints.get(stationIndex - 1);
        return STATUS_ARRIVED.equals(previousPoint.getArrivalStatus());
    }
}
