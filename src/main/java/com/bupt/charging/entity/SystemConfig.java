package com.bupt.charging.entity;

import com.bupt.charging.enums.SchedulingStrategy;
import jakarta.persistence.*;

@Entity
@Table(name = "system_config")
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "scheduling_strategy", nullable = false, length = 32)
    private SchedulingStrategy schedulingStrategy = SchedulingStrategy.TIME_ORDER;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SchedulingStrategy getSchedulingStrategy() { return schedulingStrategy; }
    public void setSchedulingStrategy(SchedulingStrategy schedulingStrategy) { this.schedulingStrategy = schedulingStrategy; }
}
