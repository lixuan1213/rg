package com.bupt.charging.enums;

/**
 * 车辆充电请求的生命周期状态。
 * <p>
 * 典型流转：WAITING → QUEUED → CHARGING → PENDING_UNPLUG → COMPLETED
 */
public enum CarState {
    /** 等候区：已提交申请，等待系统调度分配充电桩 */
    WAITING,
    /** 已分配到充电桩，在桩侧排队，等待用户插入充电头 */
    QUEUED,
    /** 充电中：用户已插入充电头，桩正在供电 */
    CHARGING,
    /** 充电完成但未拔枪：电量已达标并生成账单，等待用户拔掉充电头 */
    PENDING_UNPLUG,
    /** 已结束：用户已拔枪，请求归档 */
    COMPLETED,
    /** 已取消（预留） */
    CANCELLED
}
