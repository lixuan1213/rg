package com.bupt.charging.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ModifyAmountRequest {

    @NotBlank
    private String carId;

    @NotNull
    @Positive
    private Double amount;

    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}
