package com.example.demo.service;

import com.example.demo.dto.PageResult;
import com.example.demo.entity.ExpressCompany;
import com.example.demo.repository.ExpressCompanyRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpressCompanyService {
    private static final Logger log = LoggerFactory.getLogger(ExpressCompanyService.class);
    
    @Autowired
    private ExpressCompanyRepository expressCompanyRepository;
    
    @PostConstruct
    public void init() {
        if (expressCompanyRepository.count() == 0) {
            initDefaultCompanies();
        }
        log.info("快递公司数据加载完成，共 {} 条", expressCompanyRepository.count());
    }
    
    /**
     * 初始化默认快递公司
     */
    @Transactional
    public void initDefaultCompanies() {
        expressCompanyRepository.save(new ExpressCompany(null, "sf", "顺丰速运", "SF", 1, true, "95338", "https://www.sf-express.com"));
        expressCompanyRepository.save(new ExpressCompany(null, "zto", "中通快递", "ZTO", 2, true, "95311", "https://www.zto.com"));
        expressCompanyRepository.save(new ExpressCompany(null, "yto", "圆通速递", "YTO", 3, true, "95554", "https://www.yto.com.cn"));
        expressCompanyRepository.save(new ExpressCompany(null, "yd", "韵达快递", "YD", 4, true, "95546", "https://www.yundaex.com"));
        expressCompanyRepository.save(new ExpressCompany(null, "sto", "申通快递", "STO", 5, true, "95543", "https://www.sto.cn"));
        expressCompanyRepository.save(new ExpressCompany(null, "jd", "京东物流", "JD", 6, true, "95118", "https://www.jd.com"));
        expressCompanyRepository.save(new ExpressCompany(null, "deppon", "德邦快递", "DEPPON", 7, true, "95353", "https://www.deppon.com"));
        log.info("初始化默认快递公司完成");
    }
    
    /**
     * 获取所有启用的快递公司
     */
    public List<ExpressCompany> getEnabledCompanies() {
        return expressCompanyRepository.findByEnabledTrueOrderBySortOrderAsc();
    }
    
    /**
     * 分页查询
     */
    public PageResult<ExpressCompany> getCompanies(int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by("sortOrder"));
        Page<ExpressCompany> pageResult = expressCompanyRepository.findAll(pageRequest);
        return new PageResult<>(pageResult.getContent(), (int) pageResult.getTotalElements());
    }
    
    /**
     * 创建
     */
    @Transactional
    public ExpressCompany create(ExpressCompany company) {
        company.setCreateTime(LocalDateTime.now());
        company.setUpdateTime(LocalDateTime.now());
        return expressCompanyRepository.save(company);
    }
    
    /**
     * 更新
     */
    @Transactional
    public ExpressCompany update(Long id, ExpressCompany company) {
        ExpressCompany existing = expressCompanyRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("快递公司不存在"));
        
        existing.setName(company.getName());
        existing.setTrackingPrefix(company.getTrackingPrefix());
        existing.setSortOrder(company.getSortOrder());
        existing.setEnabled(company.getEnabled());
        existing.setPhone(company.getPhone());
        existing.setWebsite(company.getWebsite());
        existing.setUpdateTime(LocalDateTime.now());
        return expressCompanyRepository.save(existing);
    }
    
    /**
     * 删除
     */
    @Transactional
    public void delete(Long id) {
        expressCompanyRepository.deleteById(id);
    }
    
    /**
     * 根据代码获取快递公司
     */
    public ExpressCompany getByCode(String code) {
        return expressCompanyRepository.findByCode(code).orElse(null);
    }
}
