package com.bupt.charging.enums;

/**
 * 充电调度策略。
 */
public enum SchedulingStrategy {
    /** 时间顺序：先申请先调度（FIFO） */
    TIME_ORDER,
    /** 优先级：priority 高者优先，相同则按申请时间 */
    PRIORITY,
    /** 最短完成时间：优先调度充电时长短的车辆，并分配到等待+充电总时间最短的桩 */
    SHORTEST_TIME
}
