package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, length = 50)
    private String orderNo;
    
    @Column(length = 100)
    private String cargoName;
    
    @Column(length = 50)
    private String cargoType;
    
    private Double cargoWeight;    // 重量(kg)
    private Double cargoVolume;    // 体积(m³)
    private Integer cargoQuantity; // 数量(件)
    
    @Column(length = 500)
    private String remark;         // 备注
    
    @Column(length = 50)
    private String expressCompany; // 快递公司代码
    
    @Transient  // 不存数据库，动态填充
    private String expressCompanyName; // 快递公司名称
    
    @Column(length = 255)
    private String origin;
    
    @Column(length = 255)
    private String destination;
    
    @Column(length = 50)
    private String senderName;
    
    @Column(length = 50)
    private String receiverName;
    
    @Column(length = 20)
    private String senderPhone;
    
    @Column(length = 20)
    private String receiverPhone;
    
    @Column(length = 20)
    private String status;
    
    @Column(length = 50)
    private String createTime;
    
    @Column(length = 50)
    private String trackingNo;  // 运单号（发货时生成）
    
    private Integer duration;  // 预计时长(秒)
    
    @Column(columnDefinition = "TEXT")
    private String trackPointsJson;  // 轨迹点JSON存储
    
    private LocalDateTime shipTime;     // 发货时间
    private LocalDateTime receiveTime;  // 签收时间
    private LocalDateTime cancelTime;   // 取消时间
    private Double originLng;   // 发货地经度
    private Double originLat;   // 发货地纬度
    private Double destLng;     // 收货地经度
    private Double destLat;     // 收货地纬度

    public Order() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getCargoName() { return cargoName; }
    public void setCargoName(String cargoName) { this.cargoName = cargoName; }
    public String getCargoType() { return cargoType; }
    public void setCargoType(String cargoType) { this.cargoType = cargoType; }
    public Double getCargoWeight() { return cargoWeight; }
    public void setCargoWeight(Double cargoWeight) { this.cargoWeight = cargoWeight; }
    public Double getCargoVolume() { return cargoVolume; }
    public void setCargoVolume(Double cargoVolume) { this.cargoVolume = cargoVolume; }
    public Integer getCargoQuantity() { return cargoQuantity; }
    public void setCargoQuantity(Integer cargoQuantity) { this.cargoQuantity = cargoQuantity; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getExpressCompany() { return expressCompany; }
    public void setExpressCompany(String expressCompany) { this.expressCompany = expressCompany; }
    public String getExpressCompanyName() { return expressCompanyName; }
    public void setExpressCompanyName(String expressCompanyName) { this.expressCompanyName = expressCompanyName; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getSenderPhone() { return senderPhone; }
    public void setSenderPhone(String senderPhone) { this.senderPhone = senderPhone; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public String getTrackPointsJson() { return trackPointsJson; }
    public void setTrackPointsJson(String trackPointsJson) { this.trackPointsJson = trackPointsJson; }
    public LocalDateTime getShipTime() { return shipTime; }
    public void setShipTime(LocalDateTime shipTime) { this.shipTime = shipTime; }
    public LocalDateTime getReceiveTime() { return receiveTime; }
    public void setReceiveTime(LocalDateTime receiveTime) { this.receiveTime = receiveTime; }
    public LocalDateTime getCancelTime() { return cancelTime; }
    public void setCancelTime(LocalDateTime cancelTime) { this.cancelTime = cancelTime; }
    public Double getOriginLng() { return originLng; }
    public void setOriginLng(Double originLng) { this.originLng = originLng; }
    public Double getOriginLat() { return originLat; }
    public void setOriginLat(Double originLat) { this.originLat = originLat; }
    public Double getDestLng() { return destLng; }
    public void setDestLng(Double destLng) { this.destLng = destLng; }
    public Double getDestLat() { return destLat; }
    public void setDestLat(Double destLat) { this.destLat = destLat; }
}
