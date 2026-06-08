package com.bupt.charging.dto.response;

import com.bupt.charging.enums.CarState;
import com.bupt.charging.enums.ChargingMode;

import java.time.LocalDateTime;

public class ChargingStateResponse {

    private final String carId;
    private final String chargePileNum;
    private final ChargingMode requestMode;
    private final Double requestAmount;
    private final Double chargedAmount;
    private final CarState carState;
    private final LocalDateTime startTime;
    private final Long estimatedRemainingMinutes;

    public ChargingStateResponse(String carId, String chargePileNum, ChargingMode requestMode,
                                 Double requestAmount, Double chargedAmount, CarState carState,
                                 LocalDateTime startTime, Long estimatedRemainingMinutes) {
        this.carId = carId;
        this.chargePileNum = chargePileNum;
        this.requestMode = requestMode;
        this.requestAmount = requestAmount;
        this.chargedAmount = chargedAmount;
        this.carState = carState;
        this.startTime = startTime;
        this.estimatedRemainingMinutes = estimatedRemainingMinutes;
    }

    public String getCarId() { return carId; }
    public String getChargePileNum() { return chargePileNum; }
    public ChargingMode getRequestMode() { return requestMode; }
    public Double getRequestAmount() { return requestAmount; }
    public Double getChargedAmount() { return chargedAmount; }
    public CarState getCarState() { return carState; }
    public LocalDateTime getStartTime() { return startTime; }
    public Long getEstimatedRemainingMinutes() { return estimatedRemainingMinutes; }
}
