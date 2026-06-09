package com.bupt.charging.dto.response;

import com.bupt.charging.enums.CarState;

import java.time.LocalDateTime;

public class CarStateResponse {

    private final Long carNumberBeforePosition;
    private final CarState carState;
    private final String pileId;
    private final Integer queueNum;
    private final LocalDateTime requestTime;
    private final String reminderMessage;

    public CarStateResponse(Long carNumberBeforePosition, CarState carState, String pileId,
                            Integer queueNum, LocalDateTime requestTime, String reminderMessage) {
        this.carNumberBeforePosition = carNumberBeforePosition;
        this.carState = carState;
        this.pileId = pileId;
        this.queueNum = queueNum;
        this.requestTime = requestTime;
        this.reminderMessage = reminderMessage;
    }

    public Long getCarNumberBeforePosition() { return carNumberBeforePosition; }
    public CarState getCarState() { return carState; }
    public String getPileId() { return pileId; }
    public Integer getQueueNum() { return queueNum; }
    public LocalDateTime getRequestTime() { return requestTime; }
    public String getReminderMessage() { return reminderMessage; }
}
