package com.bupt.charging.dto.response;

import com.bupt.charging.enums.CarState;

import java.time.LocalDateTime;

public class CarStateResponse {

    private final Long carNumberBeforePosition;
    private final CarState carState;
    private final Integer queueNum;
    private final LocalDateTime requestTime;

    public CarStateResponse(Long carNumberBeforePosition, CarState carState,
                            Integer queueNum, LocalDateTime requestTime) {
        this.carNumberBeforePosition = carNumberBeforePosition;
        this.carState = carState;
        this.queueNum = queueNum;
        this.requestTime = requestTime;
    }

    public Long getCarNumberBeforePosition() { return carNumberBeforePosition; }
    public CarState getCarState() { return carState; }
    public Integer getQueueNum() { return queueNum; }
    public LocalDateTime getRequestTime() { return requestTime; }
}
