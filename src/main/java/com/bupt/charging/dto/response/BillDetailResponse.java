package com.bupt.charging.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BillDetailResponse {

    private final String carId;
    private final LocalDate date;
    private final Long billId;
    private final String chargePileNum;
    private final Double chargeAmount;
    private final Long chargeDuration;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final Double chargeFee;
    private final Double serviceFee;
    private final Double subtotalFee;

    public BillDetailResponse(String carId, LocalDate date, Long billId, String chargePileNum,
                              Double chargeAmount, Long chargeDuration, LocalDateTime startTime,
                              LocalDateTime endTime, Double chargeFee, Double serviceFee,
                              Double subtotalFee) {
        this.carId = carId;
        this.date = date;
        this.billId = billId;
        this.chargePileNum = chargePileNum;
        this.chargeAmount = chargeAmount;
        this.chargeDuration = chargeDuration;
        this.startTime = startTime;
        this.endTime = endTime;
        this.chargeFee = chargeFee;
        this.serviceFee = serviceFee;
        this.subtotalFee = subtotalFee;
    }

    public String getCarId() { return carId; }
    public LocalDate getDate() { return date; }
    public Long getBillId() { return billId; }
    public String getChargePileNum() { return chargePileNum; }
    public Double getChargeAmount() { return chargeAmount; }
    public Long getChargeDuration() { return chargeDuration; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Double getChargeFee() { return chargeFee; }
    public Double getServiceFee() { return serviceFee; }
    public Double getSubtotalFee() { return subtotalFee; }
}
