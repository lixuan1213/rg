package com.bupt.charging.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public class StartChargingRequest {

    @NotBlank
    private String carId;

    /** 可选；若填写必须与系统分配的充电桩一致 */
    @JsonAlias("chargingPileNum")
    private String chargePileNum;

    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }
    public String getChargePileNum() { return chargePileNum; }
    public void setChargePileNum(String chargePileNum) { this.chargePileNum = chargePileNum; }
}
