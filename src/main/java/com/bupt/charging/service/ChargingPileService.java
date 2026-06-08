package com.bupt.charging.service;

import com.bupt.charging.dto.request.SetParametersRequest;
import com.bupt.charging.dto.response.PileStateResponse;
import com.bupt.charging.dto.response.ResultResponse;
import com.bupt.charging.entity.BillingRule;
import com.bupt.charging.entity.ChargingPile;
import com.bupt.charging.enums.PileWorkingState;
import com.bupt.charging.repository.ChargingPileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChargingPileService {

    private final ChargingPileRepository chargingPileRepository;
    private final BillingService billingService;
    private final SchedulingService schedulingService;

    public ChargingPileService(ChargingPileRepository chargingPileRepository,
                               BillingService billingService,
                               SchedulingService schedulingService) {
        this.chargingPileRepository = chargingPileRepository;
        this.billingService = billingService;
        this.schedulingService = schedulingService;
    }

    @Transactional
    public ResultResponse powerOn(String pileId) {
        ChargingPile pile = getPile(pileId);
        if (pile.getWorkingState() == PileWorkingState.FAULT) {
            return ResultResponse.fail();
        }
        pile.setWorkingState(PileWorkingState.IDLE);
        chargingPileRepository.save(pile);
        return ResultResponse.success();
    }

    @Transactional
    public ResultResponse powerOff(String pileId) {
        ChargingPile pile = getPile(pileId);
        pile.setWorkingState(PileWorkingState.OFF);
        chargingPileRepository.save(pile);
        return ResultResponse.success();
    }

    @Transactional
    public ResultResponse startChargingPile(String pileId) {
        ChargingPile pile = getPile(pileId);
        if (pile.getWorkingState() == PileWorkingState.OFF
                || pile.getWorkingState() == PileWorkingState.FAULT) {
            return ResultResponse.fail();
        }
        pile.setWorkingState(PileWorkingState.CHARGING);
        chargingPileRepository.save(pile);
        schedulingService.dispatchWaitingCars(pile.getMode());
        return ResultResponse.success();
    }

    @Transactional
    public ResultResponse setParameters(SetParametersRequest request) {
        List<BillingRule> rules = request.getPriceRules().stream().map(dto -> {
            BillingRule rule = new BillingRule();
            rule.setTimePeriod(dto.getTimePeriod());
            rule.setStartHour(dto.getStartHour());
            rule.setEndHour(dto.getEndHour());
            rule.setElectricityPrice(dto.getElectricityPrice());
            rule.setServiceFeeRate(dto.getServiceFeeRate());
            return rule;
        }).toList();
        billingService.updateBillingRules(rules);
        if (request.getSchedulingStrategy() != null) {
            schedulingService.updateSchedulingStrategy(request.getSchedulingStrategy());
        }
        return ResultResponse.success();
    }

    public PileStateResponse queryPileState(String pileId) {
        ChargingPile pile = getPile(pileId);
        return new PileStateResponse(
                pile.getPileId(),
                pile.getWorkingState(),
                pile.getTotalChargeNum(),
                pile.getTotalChargeTime(),
                pile.getTotalCapacity()
        );
    }

    public List<PileStateResponse> queryAllPileStates() {
        return chargingPileRepository.findAll().stream()
                .map(pile -> new PileStateResponse(
                        pile.getPileId(),
                        pile.getWorkingState(),
                        pile.getTotalChargeNum(),
                        pile.getTotalChargeTime(),
                        pile.getTotalCapacity()
                ))
                .toList();
    }

    @Transactional
    public ResultResponse reportFault(String pileId) {
        schedulingService.handlePileFault(pileId);
        return ResultResponse.success();
    }

    @Transactional
    public ResultResponse recoverFault(String pileId) {
        schedulingService.recoverPileFault(pileId);
        return ResultResponse.success();
    }

    public ChargingPile getPile(String pileId) {
        return chargingPileRepository.findById(pileId)
                .orElseThrow(() -> new IllegalArgumentException("充电桩不存在: " + pileId));
    }

    @Transactional
    public void savePile(ChargingPile pile) {
        chargingPileRepository.save(pile);
    }
}
