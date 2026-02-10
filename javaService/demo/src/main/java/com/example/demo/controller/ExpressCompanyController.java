package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PageResult;
import com.example.demo.entity.ExpressCompany;
import com.example.demo.service.ExpressCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/express-companies")
@CrossOrigin(origins = "*")
public class ExpressCompanyController {
    
    @Autowired
    private ExpressCompanyService expressCompanyService;
    
    /**
     * 获取所有快递公司（分页）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<ExpressCompany>>> getCompanies(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<ExpressCompany> result = expressCompanyService.getCompanies(page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 获取所有启用的快递公司
     */
    @GetMapping("/enabled")
    public ResponseEntity<ApiResponse<List<ExpressCompany>>> getEnabledCompanies() {
        List<ExpressCompany> result = expressCompanyService.getEnabledCompanies();
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 创建快递公司
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ExpressCompany>> create(@RequestBody ExpressCompany company) {
        try {
            ExpressCompany created = expressCompanyService.create(company);
            return ResponseEntity.ok(ApiResponse.success("创建成功", created));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    /**
     * 更新快递公司
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpressCompany>> update(
            @PathVariable Long id,
            @RequestBody ExpressCompany company) {
        try {
            ExpressCompany updated = expressCompanyService.update(id, company);
            return ResponseEntity.ok(ApiResponse.success("更新成功", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    /**
     * 删除快递公司
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            expressCompanyService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }
}
