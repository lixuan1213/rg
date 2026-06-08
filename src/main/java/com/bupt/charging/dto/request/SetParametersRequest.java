package com.bupt.charging.dto.request;

import com.bupt.charging.enums.SchedulingStrategy;
import com.bupt.charging.enums.TimePeriod;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class SetParametersRequest {

    private SchedulingStrategy schedulingStrategy;

    @NotNull
    private List<PriceRuleDto> priceRules;

    public SchedulingStrategy getSchedulingStrategy() { return schedulingStrategy; }
    public void setSchedulingStrategy(SchedulingStrategy schedulingStrategy) { this.schedulingStrategy = schedulingStrategy; }
    public List<PriceRuleDto> getPriceRules() { return priceRules; }
    public void setPriceRules(List<PriceRuleDto> priceRules) { this.priceRules = priceRules; }

    public static class PriceRuleDto {
        @NotNull
        private TimePeriod timePeriod;
        @NotNull
        private Integer startHour;
        @NotNull
        private Integer endHour;
        @NotNull
        private Double electricityPrice;
        @NotNull
        private Double serviceFeeRate;

        public TimePeriod getTimePeriod() { return timePeriod; }
        public void setTimePeriod(TimePeriod timePeriod) { this.timePeriod = timePeriod; }
        public Integer getStartHour() { return startHour; }
        public void setStartHour(Integer startHour) { this.startHour = startHour; }
        public Integer getEndHour() { return endHour; }
        public void setEndHour(Integer endHour) { this.endHour = endHour; }
        public Double getElectricityPrice() { return electricityPrice; }
        public void setElectricityPrice(Double electricityPrice) { this.electricityPrice = electricityPrice; }
        public Double getServiceFeeRate() { return serviceFeeRate; }
        public void setServiceFeeRate(Double serviceFeeRate) { this.serviceFeeRate = serviceFeeRate; }
    }
}
