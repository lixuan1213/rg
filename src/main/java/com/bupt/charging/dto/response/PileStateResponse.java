package com.bupt.charging.dto.response;

import com.bupt.charging.enums.PileWorkingState;

public class PileStateResponse {

    private final String pileId;
    private final PileWorkingState workingState;
    private final Integer totalChargeNum;
    private final Long totalChargeTime;
    private final Double totalCapacity;

    public PileStateResponse(String pileId, PileWorkingState workingState, Integer totalChargeNum,
                           Long totalChargeTime, Double totalCapacity) {
        this.pileId = pileId;
        this.workingState = workingState;
        this.totalChargeNum = totalChargeNum;
        this.totalChargeTime = totalChargeTime;
        this.totalCapacity = totalCapacity;
    }

    public String getPileId() { return pileId; }
    public PileWorkingState getWorkingState() { return workingState; }
    public Integer getTotalChargeNum() { return totalChargeNum; }
    public Long getTotalChargeTime() { return totalChargeTime; }
    public Double getTotalCapacity() { return totalCapacity; }
}
