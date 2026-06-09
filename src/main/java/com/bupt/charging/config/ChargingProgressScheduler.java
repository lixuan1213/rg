package com.bupt.charging.config;

import com.bupt.charging.service.ChargingRequestService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ChargingProgressScheduler {

    private final ChargingRequestService chargingRequestService;

    public ChargingProgressScheduler(ChargingRequestService chargingRequestService) {
        this.chargingRequestService = chargingRequestService;
    }

    @Scheduled(fixedRate = 5000)
    public void updateChargingProgress() {
        chargingRequestService.refreshChargingProgress();
    }
}
