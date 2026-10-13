package com.campusgrid.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCampusRequest {

    @NotBlank(message = "Campus name is required.")
    @Size(max = 100, message = "Campus name must not exceed 100 characters.")
    private String name;

    @NotBlank(message = "Campus code is required.")
    @Size(max = 20, message = "Campus code must not exceed 20 characters.")
    private String code;

    @Size(max = 255, message = "Address must not exceed 255 characters.")
    private String address;

    @Size(max = 500, message = "Description must not exceed 500 characters.")
    private String description;

    public CreateCampusRequest() {
    }

    public CreateCampusRequest(String name, String code, String address, String description) {
        this.name = name;
        this.code = code;
        this.address = address;
        this.description = description;
    }

    public CreateCampusRequest(String name, String code, String address) {
        this(name, code, address, null);
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
