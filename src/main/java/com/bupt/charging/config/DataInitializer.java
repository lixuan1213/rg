package com.bupt.charging.config;

import com.bupt.charging.entity.BillingRule;
import com.bupt.charging.entity.ChargingPile;
import com.bupt.charging.entity.SystemConfig;
import com.bupt.charging.enums.ChargingMode;
import com.bupt.charging.enums.PileWorkingState;
import com.bupt.charging.enums.SchedulingStrategy;
import com.bupt.charging.enums.TimePeriod;
import com.bupt.charging.repository.BillingRuleRepository;
import com.bupt.charging.repository.ChargingPileRepository;
import com.bupt.charging.repository.SystemConfigRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    private final ChargingPileRepository chargingPileRepository;
    private final BillingRuleRepository billingRuleRepository;
    private final SystemConfigRepository systemConfigRepository;

    public DataInitializer(ChargingPileRepository chargingPileRepository,
                           BillingRuleRepository billingRuleRepository,
                           SystemConfigRepository systemConfigRepository) {
        this.chargingPileRepository = chargingPileRepository;
        this.billingRuleRepository = billingRuleRepository;
        this.systemConfigRepository = systemConfigRepository;
    }

    @Bean
    CommandLineRunner initData() {
        return args -> {
            if (chargingPileRepository.count() == 0) {
                initChargingPiles();
            }
            if (billingRuleRepository.count() == 0) {
                initBillingRules();
            }
            if (systemConfigRepository.count() == 0) {
                SystemConfig config = new SystemConfig();
                config.setSchedulingStrategy(SchedulingStrategy.TIME_ORDER);
                systemConfigRepository.save(config);
            }
        };
    }

    private void initChargingPiles() {
        for (int i = 1; i <= 2; i++) {
            ChargingPile pile = new ChargingPile();
            pile.setPileId("F" + i);
            pile.setMode(ChargingMode.FAST);
            pile.setWorkingState(PileWorkingState.IDLE);
            pile.setChargingPower(60.0);
            pile.setParkingSpots(4);
            chargingPileRepository.save(pile);
        }
        for (int i = 1; i <= 3; i++) {
            ChargingPile pile = new ChargingPile();
            pile.setPileId("S" + i);
            pile.setMode(ChargingMode.SLOW);
            pile.setWorkingState(PileWorkingState.IDLE);
            pile.setChargingPower(7.0);
            pile.setParkingSpots(4);
            chargingPileRepository.save(pile);
        }
    }

    private void initBillingRules() {
        billingRuleRepository.save(createRule(TimePeriod.PEAK, 8, 12, 1.8, 0.15));
        billingRuleRepository.save(createRule(TimePeriod.PEAK, 18, 22, 1.8, 0.15));
        billingRuleRepository.save(createRule(TimePeriod.NORMAL, 12, 18, 1.2, 0.10));
        billingRuleRepository.save(createRule(TimePeriod.VALLEY, 22, 8, 0.6, 0.08));
    }

    private BillingRule createRule(TimePeriod period, int start, int end,
                                   double price, double serviceRate) {
        BillingRule rule = new BillingRule();
        rule.setTimePeriod(period);
        rule.setStartHour(start);
        rule.setEndHour(end);
        rule.setElectricityPrice(price);
        rule.setServiceFeeRate(serviceRate);
        return rule;
    }
}
