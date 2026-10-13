package com.campusgrid.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateBlockRequest {

    @NotBlank(message = "Block name is required.")
    @Size(max = 100, message = "Block name must not exceed 100 characters.")
    private String name;

    @NotBlank(message = "Block code is required.")
    @Size(max = 20, message = "Block code must not exceed 20 characters.")
    private String code;

    @Size(max = 500, message = "Description must not exceed 500 characters.")
    private String description;

    private Long campusId;

    public CreateBlockRequest() {
    }

    public CreateBlockRequest(Long campusId, String name, String code, String description) {
        this.campusId = campusId;
        this.name = name;
        this.code = code;
        this.description = description;
    }

    public CreateBlockRequest(String name, String code, String description) {
        this(null, name, code, description);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCampusId() {
        return campusId;
    }

    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }
}
