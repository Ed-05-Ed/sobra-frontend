package com.sobra.inventory.api.dto;

import java.time.LocalDate;

import com.sobra.inventory.model.DateType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class UpdateFoodRequest {

    @NotNull
    @PositiveOrZero
    private Long version;

    private String name;
    private boolean namePresent;

    private LocalDate labelDate;
    private boolean labelDatePresent;

    private DateType dateType;
    private boolean dateTypePresent;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.namePresent = true;
        this.name = name;
    }

    public boolean hasName() {
        return namePresent;
    }

    public LocalDate getLabelDate() {
        return labelDate;
    }

    public void setLabelDate(LocalDate labelDate) {
        this.labelDatePresent = true;
        this.labelDate = labelDate;
    }

    public boolean hasLabelDate() {
        return labelDatePresent;
    }

    public DateType getDateType() {
        return dateType;
    }

    public void setDateType(DateType dateType) {
        this.dateTypePresent = true;
        this.dateType = dateType;
    }

    public boolean hasDateType() {
        return dateTypePresent;
    }
}
