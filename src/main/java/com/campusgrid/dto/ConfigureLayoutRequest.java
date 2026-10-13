package com.campusgrid.dto;

import com.campusgrid.model.LayoutTemplate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ConfigureLayoutRequest {

    @NotNull(message = "Layout template is required.")
    private LayoutTemplate layoutTemplate;

    @NotNull(message = "Rows count is required.")
    @Min(value = 1, message = "Rows count must be at least 1.")
    private Integer rowsCount;

    @NotNull(message = "Columns count is required.")
    @Min(value = 1, message = "Columns count must be at least 1.")
    private Integer columnsCount;

    private Integer systemCount;

    public ConfigureLayoutRequest() {
    }

    public ConfigureLayoutRequest(LayoutTemplate layoutTemplate, Integer rowsCount, Integer columnsCount, Integer systemCount) {
        this.layoutTemplate = layoutTemplate;
        this.rowsCount = rowsCount;
        this.columnsCount = columnsCount;
        this.systemCount = systemCount;
    }

    public LayoutTemplate getLayoutTemplate() {
        return layoutTemplate;
    }

    public void setLayoutTemplate(LayoutTemplate layoutTemplate) {
        this.layoutTemplate = layoutTemplate;
    }

    public Integer getRowsCount() {
        return rowsCount;
    }

    public void setRowsCount(Integer rowsCount) {
        this.rowsCount = rowsCount;
    }

    public Integer getColumnsCount() {
        return columnsCount;
    }

    public void setColumnsCount(Integer columnsCount) {
        this.columnsCount = columnsCount;
    }

    public Integer getSystemCount() {
        return systemCount;
    }

    public void setSystemCount(Integer systemCount) {
        this.systemCount = systemCount;
    }
}
