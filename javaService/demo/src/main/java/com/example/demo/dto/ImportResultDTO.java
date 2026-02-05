package com.example.demo.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入结果
 */
public class ImportResultDTO {
    private int total;      // 总记录数
    private int success;    // 成功数
    private int failed;     // 失败数
    private List<ImportError> errors;

    public ImportResultDTO() {
        this.errors = new ArrayList<>();
    }

    public ImportResultDTO(int total, int success, int failed, List<ImportError> errors) {
        this.total = total;
        this.success = success;
        this.failed = failed;
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    // Getters and Setters
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getSuccess() { return success; }
    public void setSuccess(int success) { this.success = success; }

    public int getFailed() { return failed; }
    public void setFailed(int failed) { this.failed = failed; }

    public List<ImportError> getErrors() { return errors; }
    public void setErrors(List<ImportError> errors) { 
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    /**
     * 添加错误记录
     */
    public void addError(ImportError error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }
}
