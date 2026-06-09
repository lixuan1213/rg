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
    private final Long elapsedSeconds;
    private final Long estimatedRemainingMinutes;
    private final String reminderMessage;

    public ChargingStateResponse(String carId, String chargePileNum, ChargingMode requestMode,
                                 Double requestAmount, Double chargedAmount, CarState carState,
                                 LocalDateTime startTime, Long elapsedSeconds,
                                 Long estimatedRemainingMinutes, String reminderMessage) {
        this.carId = carId;
        this.chargePileNum = chargePileNum;
        this.requestMode = requestMode;
        this.requestAmount = requestAmount;
        this.chargedAmount = chargedAmount;
        this.carState = carState;
        this.startTime = startTime;
        this.elapsedSeconds = elapsedSeconds;
        this.estimatedRemainingMinutes = estimatedRemainingMinutes;
        this.reminderMessage = reminderMessage;
    }

    public String getCarId() { return carId; }
    public String getChargePileNum() { return chargePileNum; }
    public ChargingMode getRequestMode() { return requestMode; }
    public Double getRequestAmount() { return requestAmount; }
    public Double getChargedAmount() { return chargedAmount; }
    public CarState getCarState() { return carState; }
    public LocalDateTime getStartTime() { return startTime; }
    public Long getElapsedSeconds() { return elapsedSeconds; }
    public Long getEstimatedRemainingMinutes() { return estimatedRemainingMinutes; }
    public String getReminderMessage() { return reminderMessage; }
}
