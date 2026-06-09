package com.bupt.charging.entity;

import com.bupt.charging.enums.CarState;
import com.bupt.charging.enums.ChargingMode;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "charging_request")
public class ChargingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "car_id", nullable = false, length = 32)
    private String carId;

    @Column(name = "request_amount", nullable = false)
    private Double requestAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_mode", nullable = false, length = 16)
    private ChargingMode requestMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "car_state", nullable = false, length = 16)
    private CarState carState = CarState.WAITING;

    @Column(name = "queue_num")
    private Integer queueNum;

    @Column(name = "car_position")
    private Integer carPosition;

    @Column(name = "request_time", nullable = false)
    private LocalDateTime requestTime;

    @Column(name = "pile_id", length = 32)
    private String pileId;

    @Column(name = "car_capacity")
    private Double carCapacity;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    @Column(name = "charged_amount", nullable = false)
    private Double chargedAmount = 0.0;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "bill_id")
    private Long billId;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }
    public Double getRequestAmount() { return requestAmount; }
    public void setRequestAmount(Double requestAmount) { this.requestAmount = requestAmount; }
    public ChargingMode getRequestMode() { return requestMode; }
    public void setRequestMode(ChargingMode requestMode) { this.requestMode = requestMode; }
    public CarState getCarState() { return carState; }
    public void setCarState(CarState carState) { this.carState = carState; }
    public Integer getQueueNum() { return queueNum; }
    public void setQueueNum(Integer queueNum) { this.queueNum = queueNum; }
    public Integer getCarPosition() { return carPosition; }
    public void setCarPosition(Integer carPosition) { this.carPosition = carPosition; }
    public LocalDateTime getRequestTime() { return requestTime; }
    public void setRequestTime(LocalDateTime requestTime) { this.requestTime = requestTime; }
    public String getPileId() { return pileId; }
    public void setPileId(String pileId) { this.pileId = pileId; }
    public Double getCarCapacity() { return carCapacity; }
    public void setCarCapacity(Double carCapacity) { this.carCapacity = carCapacity; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public Double getChargedAmount() { return chargedAmount; }
    public void setChargedAmount(Double chargedAmount) { this.chargedAmount = chargedAmount; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
