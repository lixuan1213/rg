package com.bupt.charging.dto.request;

import com.bupt.charging.enums.ChargingMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ModifyModeRequest {

    @NotBlank
    private String carId;

    @NotNull
    private ChargingMode mode;

    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }
    public ChargingMode getMode() { return mode; }
    public void setMode(ChargingMode mode) { this.mode = mode; }
}
