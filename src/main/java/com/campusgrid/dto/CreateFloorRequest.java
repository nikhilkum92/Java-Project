package com.campusgrid.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateFloorRequest {

    @NotNull(message = "Floor number is required.")
    private Integer floorNumber;

    @NotBlank(message = "Floor name is required.")
    @Size(max = 50, message = "Floor name must not exceed 50 characters.")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters.")
    private String description;

    private Long blockId;

    public CreateFloorRequest() {
    }

    public CreateFloorRequest(Long blockId, Integer floorNumber, String name, String description) {
        this.blockId = blockId;
        this.floorNumber = floorNumber;
        this.name = name;
        this.description = description;
    }

    public CreateFloorRequest(Integer floorNumber, String name, String description) {
        this(null, floorNumber, name, description);
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getBlockId() {
        return blockId;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }
}
