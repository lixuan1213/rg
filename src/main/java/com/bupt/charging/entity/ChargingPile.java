package com.bupt.charging.entity;

import com.bupt.charging.enums.ChargingMode;
import com.bupt.charging.enums.PileWorkingState;
import jakarta.persistence.*;

@Entity
@Table(name = "charging_pile")
public class ChargingPile {

    @Id
    @Column(name = "pile_id", length = 32)
    private String pileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ChargingMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "working_state", nullable = false, length = 16)
    private PileWorkingState workingState = PileWorkingState.OFF;

    @Column(name = "total_charge_num", nullable = false)
    private Integer totalChargeNum = 0;

    @Column(name = "total_charge_time", nullable = false)
    private Long totalChargeTime = 0L;

    @Column(name = "total_capacity", nullable = false)
    private Double totalCapacity = 0.0;

    @Column(name = "parking_spots", nullable = false)
    private Integer parkingSpots = 4;

    @Column(name = "occupied_spots", nullable = false)
    private Integer occupiedSpots = 0;

    @Column(name = "charging_power", nullable = false)
    private Double chargingPower;

    public String getPileId() { return pileId; }
    public void setPileId(String pileId) { this.pileId = pileId; }
    public ChargingMode getMode() { return mode; }
    public void setMode(ChargingMode mode) { this.mode = mode; }
    public PileWorkingState getWorkingState() { return workingState; }
    public void setWorkingState(PileWorkingState workingState) { this.workingState = workingState; }
    public Integer getTotalChargeNum() { return totalChargeNum; }
    public void setTotalChargeNum(Integer totalChargeNum) { this.totalChargeNum = totalChargeNum; }
    public Long getTotalChargeTime() { return totalChargeTime; }
    public void setTotalChargeTime(Long totalChargeTime) { this.totalChargeTime = totalChargeTime; }
    public Double getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(Double totalCapacity) { this.totalCapacity = totalCapacity; }
    public Integer getParkingSpots() { return parkingSpots; }
    public void setParkingSpots(Integer parkingSpots) { this.parkingSpots = parkingSpots; }
    public Integer getOccupiedSpots() { return occupiedSpots; }
    public void setOccupiedSpots(Integer occupiedSpots) { this.occupiedSpots = occupiedSpots; }
    public Double getChargingPower() { return chargingPower; }
    public void setChargingPower(Double chargingPower) { this.chargingPower = chargingPower; }
}
