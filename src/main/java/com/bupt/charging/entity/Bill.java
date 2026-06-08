package com.bupt.charging.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bill")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_id")
    private Long billId;

    @Column(name = "car_id", nullable = false, length = 32)
    private String carId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "charge_pile_num", nullable = false, length = 32)
    private String chargePileNum;

    @Column(name = "charge_amount", nullable = false)
    private Double chargeAmount;

    @Column(name = "charge_duration", nullable = false)
    private Long chargeDuration;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "charge_fee", nullable = false)
    private Double chargeFee;

    @Column(name = "service_fee", nullable = false)
    private Double serviceFee;

    @Column(name = "total_fee", nullable = false)
    private Double totalFee;

    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }
    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getChargePileNum() { return chargePileNum; }
    public void setChargePileNum(String chargePileNum) { this.chargePileNum = chargePileNum; }
    public Double getChargeAmount() { return chargeAmount; }
    public void setChargeAmount(Double chargeAmount) { this.chargeAmount = chargeAmount; }
    public Long getChargeDuration() { return chargeDuration; }
    public void setChargeDuration(Long chargeDuration) { this.chargeDuration = chargeDuration; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Double getChargeFee() { return chargeFee; }
    public void setChargeFee(Double chargeFee) { this.chargeFee = chargeFee; }
    public Double getServiceFee() { return serviceFee; }
    public void setServiceFee(Double serviceFee) { this.serviceFee = serviceFee; }
    public Double getTotalFee() { return totalFee; }
    public void setTotalFee(Double totalFee) { this.totalFee = totalFee; }
}
