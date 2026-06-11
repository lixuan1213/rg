package com.bupt.charging.dto.response;

public class QueueStateResponse {

    private final String carId;
    private final Double carCapacity;
    private final Double requestAmount;
    private final Long waitTime;
    private final Long estimatedRemainingWaitMinutes;

    public QueueStateResponse(String carId, Double carCapacity, Double requestAmount, Long waitTime,
                              Long estimatedRemainingWaitMinutes) {
        this.carId = carId;
        this.carCapacity = carCapacity;
        this.requestAmount = requestAmount;
        this.waitTime = waitTime;
        this.estimatedRemainingWaitMinutes = estimatedRemainingWaitMinutes;
    }

    public String getCarId() { return carId; }
    public Double getCarCapacity() { return carCapacity; }
    public Double getRequestAmount() { return requestAmount; }
    public Long getWaitTime() { return waitTime; }
    public Long getEstimatedRemainingWaitMinutes() { return estimatedRemainingWaitMinutes; }
}
