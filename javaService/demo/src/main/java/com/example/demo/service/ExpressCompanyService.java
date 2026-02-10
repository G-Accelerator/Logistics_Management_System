package com.example.demo.service;

import com.example.demo.dto.PageResult;
import com.example.demo.entity.ExpressCompany;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ExpressCompanyService {
    private static final Logger log = LoggerFactory.getLogger(ExpressCompanyService.class);
    private static final String DATA_FILE = "data/express-companies.json";
    
    private final List<ExpressCompany> companies = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final ObjectMapper objectMapper;
    
    public ExpressCompanyService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    @PostConstruct
    public void init() {
        loadFromFile();
        if (companies.isEmpty()) {
            initDefaultCompanies();
            saveToFile();
        }
        log.info("快递公司数据加载完成，共 {} 条", companies.size());
    }
    
    /**
     * 初始化默认快递公司
     */
    private void initDefaultCompanies() {
        companies.add(new ExpressCompany(1L, "sf", "顺丰速运", "SF", 1, true, "95338", "https://www.sf-express.com"));
        companies.add(new ExpressCompany(2L, "zto", "中通快递", "ZTO", 2, true, "95311", "https://www.zto.com"));
        companies.add(new ExpressCompany(3L, "yto", "圆通速递", "YTO", 3, true, "95554", "https://www.yto.com.cn"));
        companies.add(new ExpressCompany(4L, "yd", "韵达快递", "YD", 4, true, "95546", "https://www.yundaex.com"));
        companies.add(new ExpressCompany(5L, "sto", "申通快递", "STO", 5, true, "95543", "https://www.sto.cn"));
        companies.add(new ExpressCompany(6L, "jd", "京东物流", "JD", 6, true, "95118", "https://www.jd.com"));
        companies.add(new ExpressCompany(7L, "deppon", "德邦快递", "DEPPON", 7, true, "95353", "https://www.deppon.com"));
        idGenerator.set(8);
    }
    
    /**
     * 从文件加载
     */
    private void loadFromFile() {
        try {
            File file = new File(DATA_FILE);
            if (!file.exists()) {
                return;
            }
            
            List<ExpressCompany> loaded = objectMapper.readValue(
                file,
                new TypeReference<List<ExpressCompany>>() {}
            );
            companies.addAll(loaded);
            
            // 更新 ID 生成器
            long maxId = companies.stream()
                .map(ExpressCompany::getId)
                .max(Long::compareTo)
                .orElse(0L);
            idGenerator.set(maxId + 1);
        } catch (IOException e) {
            log.error("加载快递公司数据失败", e);
        }
    }
    
    /**
     * 保存到文件
     */
    private void saveToFile() {
        try {
            File file = new File(DATA_FILE);
            file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, companies);
        } catch (IOException e) {
            log.error("保存快递公司数据失败", e);
        }
    }
    
    /**
     * 获取所有启用的快递公司
     */
    public List<ExpressCompany> getEnabledCompanies() {
        return companies.stream()
            .filter(ExpressCompany::getEnabled)
            .sorted(Comparator.comparingInt(ExpressCompany::getSortOrder))
            .toList();
    }
    
    /**
     * 分页查询
     */
    public PageResult<ExpressCompany> getCompanies(int page, int pageSize) {
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, companies.size());
        List<ExpressCompany> pageData = start < companies.size() ? companies.subList(start, end) : List.of();
        return new PageResult<>(pageData, companies.size());
    }
    
    /**
     * 创建
     */
    public ExpressCompany create(ExpressCompany company) {
        company.setId(idGenerator.getAndIncrement());
        company.setCreateTime(LocalDateTime.now());
        company.setUpdateTime(LocalDateTime.now());
        companies.add(company);
        saveToFile();
        return company;
    }
    
    /**
     * 更新
     */
    public ExpressCompany update(Long id, ExpressCompany company) {
        ExpressCompany existing = companies.stream()
            .filter(c -> c.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("快递公司不存在"));
        
        existing.setName(company.getName());
        existing.setTrackingPrefix(company.getTrackingPrefix());
        existing.setSortOrder(company.getSortOrder());
        existing.setEnabled(company.getEnabled());
        existing.setPhone(company.getPhone());
        existing.setWebsite(company.getWebsite());
        existing.setUpdateTime(LocalDateTime.now());
        saveToFile();
        return existing;
    }
    
    /**
     * 删除
     */
    public void delete(Long id) {
        companies.removeIf(c -> c.getId().equals(id));
        saveToFile();
    }
    
    /**
     * 根据代码获取快递公司
     */
    public ExpressCompany getByCode(String code) {
        return companies.stream()
            .filter(c -> c.getCode().equals(code))
            .findFirst()
            .orElse(null);
    }
}
