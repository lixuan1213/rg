package com.bupt.charging.dto.response;

public class QueueStateResponse {

    private final String carId;
    private final Double carCapacity;
    private final Double requestAmount;
    private final Long waitTime;

    public QueueStateResponse(String carId, Double carCapacity, Double requestAmount, Long waitTime) {
        this.carId = carId;
        this.carCapacity = carCapacity;
        this.requestAmount = requestAmount;
        this.waitTime = waitTime;
    }

    public String getCarId() { return carId; }
    public Double getCarCapacity() { return carCapacity; }
    public Double getRequestAmount() { return requestAmount; }
    public Long getWaitTime() { return waitTime; }
}
