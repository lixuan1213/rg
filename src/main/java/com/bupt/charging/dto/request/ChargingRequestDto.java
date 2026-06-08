package com.bupt.charging.dto.request;

import com.bupt.charging.enums.ChargingMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ChargingRequestDto {

    @NotBlank
    private String carId;

    @NotNull
    @Positive
    private Double requestAmount;

    @NotNull
    private ChargingMode requestMode;

    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }
    public Double getRequestAmount() { return requestAmount; }
    public void setRequestAmount(Double requestAmount) { this.requestAmount = requestAmount; }
    public ChargingMode getRequestMode() { return requestMode; }
    public void setRequestMode(ChargingMode requestMode) { this.requestMode = requestMode; }
}
