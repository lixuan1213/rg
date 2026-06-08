package com.bupt.charging.dto.request;

import jakarta.validation.constraints.NotBlank;

public class EndChargingRequest {

    @NotBlank
    private String carId;

    @NotBlank
    private String chargingPileNum;

    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }
    public String getChargingPileNum() { return chargingPileNum; }
    public void setChargingPileNum(String chargingPileNum) { this.chargingPileNum = chargingPileNum; }
}
