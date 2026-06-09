package com.bupt.charging.enums;

/**
 * 充电桩工作状态，与车辆状态配合描述桩侧物理占用情况。
 */
public enum PileWorkingState {
    /** 管理员关机，不可调度 */
    OFF,
    /** 空闲：无车辆在充或等待拔枪，可接受新充电头插入 */
    IDLE,
    /** 充电中：有车辆插入充电头并正在充电 */
    CHARGING,
    /** 等待拔枪：充电已结束，充电头仍插着，下一位用户暂不可插入 */
    WAITING_UNPLUG,
    /** 故障：暂停服务并触发再调度 */
    FAULT
}
