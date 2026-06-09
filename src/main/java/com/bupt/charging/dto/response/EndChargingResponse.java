package com.bupt.charging.dto.response;

public class EndChargingResponse {

    private final int result;
    private final String message;
    private final Long billId;
    private final String chargePileNum;
    private final Double chargeAmount;
    private final Long chargeDuration;
    private final Double totalFee;

    public EndChargingResponse(int result, String message, Long billId, String chargePileNum,
                               Double chargeAmount, Long chargeDuration, Double totalFee) {
        this.result = result;
        this.message = message;
        this.billId = billId;
        this.chargePileNum = chargePileNum;
        this.chargeAmount = chargeAmount;
        this.chargeDuration = chargeDuration;
        this.totalFee = totalFee;
    }

    public static EndChargingResponse success(Long billId, String chargePileNum,
                                              Double chargeAmount, Long chargeDuration,
                                              Double totalFee) {
        return success("充电结束，账单已生成", billId, chargePileNum, chargeAmount, chargeDuration, totalFee);
    }

    public static EndChargingResponse success(String message, Long billId, String chargePileNum,
                                              Double chargeAmount, Long chargeDuration,
                                              Double totalFee) {
        return new EndChargingResponse(0, message, billId, chargePileNum,
                chargeAmount, chargeDuration, totalFee);
    }

    public static EndChargingResponse fail(String message) {
        return new EndChargingResponse(1, message, null, null, null, null, null);
    }

    public int getResult() { return result; }
    public String getMessage() { return message; }
    public Long getBillId() { return billId; }
    public String getChargePileNum() { return chargePileNum; }
    public Double getChargeAmount() { return chargeAmount; }
    public Long getChargeDuration() { return chargeDuration; }
    public Double getTotalFee() { return totalFee; }
}
