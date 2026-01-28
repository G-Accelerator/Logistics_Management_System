package com.example.demo.entity;

public class Order {
    private Long id;
    private String orderNo;
    private String cargoName;
    private String cargoType;
    private String origin;
    private String destination;
    private String senderName;
    private String receiverName;
    private String senderPhone;
    private String receiverPhone;
    private String status;
    private String createTime;

    public Order() {}

    public Order(Long id, String orderNo, String cargoName, String cargoType, 
                 String origin, String destination, String senderName, 
                 String receiverName, String senderPhone, String receiverPhone,
                 String status, String createTime) {
        this.id = id;
        this.orderNo = orderNo;
        this.cargoName = cargoName;
        this.cargoType = cargoType;
        this.origin = origin;
        this.destination = destination;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.senderPhone = senderPhone;
        this.receiverPhone = receiverPhone;
        this.status = status;
        this.createTime = createTime;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getCargoName() { return cargoName; }
    public void setCargoName(String cargoName) { this.cargoName = cargoName; }
    public String getCargoType() { return cargoType; }
    public void setCargoType(String cargoType) { this.cargoType = cargoType; }
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
}
