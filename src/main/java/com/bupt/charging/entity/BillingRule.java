package com.bupt.charging.entity;

import com.bupt.charging.enums.TimePeriod;
import jakarta.persistence.*;

@Entity
@Table(name = "billing_rule")
public class BillingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_period", nullable = false, length = 16)
    private TimePeriod timePeriod;

    @Column(name = "start_hour", nullable = false)
    private Integer startHour;

    @Column(name = "end_hour", nullable = false)
    private Integer endHour;

    @Column(name = "electricity_price", nullable = false)
    private Double electricityPrice;

    @Column(name = "service_fee_rate", nullable = false)
    private Double serviceFeeRate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
