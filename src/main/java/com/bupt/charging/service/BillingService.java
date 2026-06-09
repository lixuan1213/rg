package com.bupt.charging.service;

import com.bupt.charging.entity.Bill;
import com.bupt.charging.entity.BillingRule;
import com.bupt.charging.entity.ChargingPile;
import com.bupt.charging.entity.ChargingRequest;
import com.bupt.charging.enums.TimePeriod;
import com.bupt.charging.repository.BillRepository;
import com.bupt.charging.repository.BillingRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 计费服务：按结束时刻所在时段的电价规则计算充电费与服务费。
 * <p>
 * 总费用 = 充电费 + 服务费，其中服务费 = 充电费 × 服务费率。
 */
@Service
public class BillingService {

    private final BillRepository billRepository;
    private final BillingRuleRepository billingRuleRepository;

    public BillingService(BillRepository billRepository, BillingRuleRepository billingRuleRepository) {
        this.billRepository = billRepository;
        this.billingRuleRepository = billingRuleRepository;
    }

    /** 根据充电会话生成账单，充电时长不足 1 分钟按 1 分钟计 */
    public Bill createBill(ChargingRequest request, ChargingPile pile) {
        LocalDateTime start = request.getStartTime();
        LocalDateTime end = request.getEndTime() != null ? request.getEndTime() : LocalDateTime.now();
        long durationMinutes = ChronoUnit.MINUTES.between(start, end);
        if (durationMinutes <= 0) {
            durationMinutes = 1;
        }

        double chargedAmount = request.getChargedAmount();
        if (chargedAmount <= 0) {
            chargedAmount = pile.getChargingPower() * durationMinutes / 60.0;
        }

        BillingRule rule = findRuleForTime(end);
        double chargeFee = chargedAmount * rule.getElectricityPrice();
        double serviceFee = chargeFee * rule.getServiceFeeRate();

        Bill bill = new Bill();
        bill.setCarId(request.getCarId());
        bill.setDate(end.toLocalDate());
        bill.setChargePileNum(pile.getPileId());
        bill.setChargeAmount(chargedAmount);
        bill.setChargeDuration(durationMinutes);
        bill.setStartTime(start);
        bill.setEndTime(end);
        bill.setChargeFee(round(chargeFee));
        bill.setServiceFee(round(serviceFee));
        bill.setTotalFee(round(chargeFee + serviceFee));
        return billRepository.save(bill);
    }

    public List<Bill> getBills(String carId, LocalDate date) {
        return billRepository.findByCarIdAndDate(carId, date);
    }

    public Bill getBill(Long billId) {
        return billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("账单不存在: " + billId));
    }

    @Transactional
    public void updateBillingRules(List<BillingRule> rules) {
        billingRuleRepository.deleteAll();
        billingRuleRepository.saveAll(rules);
    }

    /** 按结束时刻匹配峰/平/谷时段电价规则 */
    public BillingRule findRuleForTime(LocalDateTime time) {
        int hour = time.getHour();
        return billingRuleRepository.findAll().stream()
                .filter(rule -> isInPeriod(hour, rule.getStartHour(), rule.getEndHour()))
                .findFirst()
                .orElseGet(this::defaultRule);
    }

    private boolean isInPeriod(int hour, int start, int end) {
        if (start <= end) {
            return hour >= start && hour < end;
        }
        return hour >= start || hour < end;
    }

    private BillingRule defaultRule() {
        BillingRule rule = new BillingRule();
        rule.setTimePeriod(TimePeriod.NORMAL);
        rule.setStartHour(0);
        rule.setEndHour(24);
        rule.setElectricityPrice(1.0);
        rule.setServiceFeeRate(0.1);
        return rule;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
