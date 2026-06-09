package com.bupt.charging.config;

import com.bupt.charging.service.ChargingRequestService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 后台定时任务：周期性检查充电中的车辆是否已达申请电量。
 * 达到后自动进入 {@code PENDING_UNPLUG} 状态并生成账单，等待用户拔枪。
 */
@Component
public class ChargingProgressScheduler {

    private final ChargingRequestService chargingRequestService;

    public ChargingProgressScheduler(ChargingRequestService chargingRequestService) {
        this.chargingRequestService = chargingRequestService;
    }

    /** 每 5 秒刷新一次充电进度，并在充满时触发自动结束 */
    @Scheduled(fixedRate = 5000)
    public void updateChargingProgress() {
        chargingRequestService.refreshChargingProgress();
    }
}
