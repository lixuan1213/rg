package com.bupt.charging.dto.response;

import com.bupt.charging.enums.CarState;

import java.time.LocalDateTime;

public class ChargingRequestResponse {

    private final Integer carPosition;
    private final CarState carState;
    private final Integer queueNum;
    private final LocalDateTime requestTime;

    public ChargingRequestResponse(Integer carPosition, CarState carState,
                                   Integer queueNum, LocalDateTime requestTime) {
        this.carPosition = carPosition;
        this.carState = carState;
        this.queueNum = queueNum;
        this.requestTime = requestTime;
    }

    public Integer getCarPosition() { return carPosition; }
    public CarState getCarState() { return carState; }
    public Integer getQueueNum() { return queueNum; }
    public LocalDateTime getRequestTime() { return requestTime; }
}
