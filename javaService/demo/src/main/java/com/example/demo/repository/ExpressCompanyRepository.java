package com.example.demo.repository;

import com.example.demo.entity.ExpressCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpressCompanyRepository extends JpaRepository<ExpressCompany, Long> {
    
    Optional<ExpressCompany> findByCode(String code);
    
    List<ExpressCompany> findByEnabledTrueOrderBySortOrderAsc();
    
    List<ExpressCompany> findAllByOrderBySortOrderAsc();
    
    boolean existsByCode(String code);
}
