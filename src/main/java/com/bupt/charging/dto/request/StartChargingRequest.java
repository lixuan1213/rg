package com.bupt.charging.dto.request;

import jakarta.validation.constraints.NotBlank;

public class StartChargingRequest {

    @NotBlank
    private String carId;

    @NotBlank
    private String chargePileNum;

    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }
    public String getChargePileNum() { return chargePileNum; }
    public void setChargePileNum(String chargePileNum) { this.chargePileNum = chargePileNum; }
}
