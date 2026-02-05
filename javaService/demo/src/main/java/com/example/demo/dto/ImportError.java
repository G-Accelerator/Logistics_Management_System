package com.example.demo.dto;

/**
 * 导入错误详情
 */
public class ImportError {
    private int row;        // 行号
    private String field;   // 字段名
    private String value;   // 原始值
    private String message; // 错误原因

    public ImportError() {}

    public ImportError(int row, String field, String value, String message) {
        this.row = row;
        this.field = field;
        this.value = value;
        this.message = message;
    }

    // Getters and Setters
    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
