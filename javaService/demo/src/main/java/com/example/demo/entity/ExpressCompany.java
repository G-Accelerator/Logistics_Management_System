package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 快递公司实体
 */
@Entity
@Table(name = "express_companies")
public class ExpressCompany {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, length = 20)
    private String code;              // sf, zto 等
    
    @Column(length = 50)
    private String name;              // 顺丰速运、中通快递等
    
    @Column(length = 20)
    private String trackingPrefix;    // SF, ZTO 等（用于生成运单号）
    
    private Integer sortOrder;        // 排序
    private Boolean enabled;          // 是否启用
    
    @Column(length = 20)
    private String phone;             // 客服电话
    
    @Column(length = 100)
    private String website;           // 官网
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public ExpressCompany() {}

    public ExpressCompany(Long id, String code, String name, String trackingPrefix,
                         Integer sortOrder, Boolean enabled, String phone, String website) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.trackingPrefix = trackingPrefix;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
        this.phone = phone;
        this.website = website;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTrackingPrefix() { return trackingPrefix; }
    public void setTrackingPrefix(String trackingPrefix) { this.trackingPrefix = trackingPrefix; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
