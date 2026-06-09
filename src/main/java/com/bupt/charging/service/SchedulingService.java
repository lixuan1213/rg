package com.bupt.charging.service;

import com.bupt.charging.entity.ChargingPile;
import com.bupt.charging.entity.ChargingRequest;
import com.bupt.charging.entity.SystemConfig;
import com.bupt.charging.enums.CarState;
import com.bupt.charging.enums.ChargingMode;
import com.bupt.charging.enums.PileWorkingState;
import com.bupt.charging.enums.SchedulingStrategy;
import com.bupt.charging.repository.ChargingPileRepository;
import com.bupt.charging.repository.ChargingRequestRepository;
import com.bupt.charging.repository.SystemConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 充电调度服务：负责将等候区车辆分配到充电桩，并维护桩侧车位与排队序号。
 * <p>
 * 快充/慢充分开调度；支持时间顺序、优先级、最短完成时间三种策略。
 */
@Service
public class SchedulingService {

    /** 等待拔枪的预期阻塞时间（秒），用于桩选择时的估算惩罚 */
    private static final double UNPLUG_BLOCK_ESTIMATE_SECONDS = 300.0;

    private final ChargingRequestRepository chargingRequestRepository;
    private final ChargingPileRepository chargingPileRepository;
    private final SystemConfigRepository systemConfigRepository;

    public SchedulingService(ChargingRequestRepository chargingRequestRepository,
                             ChargingPileRepository chargingPileRepository,
                             SystemConfigRepository systemConfigRepository) {
        this.chargingRequestRepository = chargingRequestRepository;
        this.chargingPileRepository = chargingPileRepository;
        this.systemConfigRepository = systemConfigRepository;
    }

    /**
     * 将等候区车辆依次分配到有空位的同模式充电桩。
     * 分配后车辆进入 QUEUED 状态，等待用户手动插入充电头。
     */
    @Transactional
    public void dispatchWaitingCars(ChargingMode mode) {
        syncPileOccupiedSpots(mode);
        List<ChargingRequest> waitingCars = getOrderedWaitingCars(mode);
        for (ChargingRequest request : waitingCars) {
            Optional<ChargingPile> pileOpt = selectPileForDispatch(mode, request);
            if (pileOpt.isEmpty()) {
                break;
            }
            assignCarToPile(request, pileOpt.get());
        }
        refreshAllPileQueueNumbers(mode);
    }

    /**
     * 根据数据库实际占用情况校正充电桩车位数与工作状态，防止计数漂移。
     */
    @Transactional
    public void syncPileOccupiedSpots(ChargingMode mode) {
        Set<CarState> occupyingStates = Set.of(
                CarState.QUEUED, CarState.CHARGING, CarState.PENDING_UNPLUG);
        for (ChargingPile pile : chargingPileRepository.findByMode(mode)) {
            int actual = (int) chargingRequestRepository.countByPileIdAndActiveTrueAndCarStateIn(
                    pile.getPileId(), occupyingStates);
            pile.setOccupiedSpots(actual);
            updatePileWorkingState(pile);
            chargingPileRepository.save(pile);
        }
    }

    /**
     * 重排桩侧 QUEUED 队列序号。
     * 最短完成时间策略下按申请电量升序（充电时长短的先充），其他策略保持原排队先后。
     */
    public void refreshPileQueueNumbers(String pileId) {
        List<ChargingRequest> queued = chargingRequestRepository
                .findByPileIdAndCarStateAndActiveTrueOrderByQueueNumAsc(pileId, CarState.QUEUED);
        if (getSchedulingStrategy() == SchedulingStrategy.SHORTEST_TIME) {
            queued = queued.stream()
                    .sorted(Comparator.comparing(ChargingRequest::getRequestAmount)
                            .thenComparing(ChargingRequest::getRequestTime))
                    .toList();
        }
        for (int i = 0; i < queued.size(); i++) {
            queued.get(i).setQueueNum(i + 1);
            chargingRequestRepository.save(queued.get(i));
        }
    }

    public void refreshAllPileQueueNumbers(ChargingMode mode) {
        chargingPileRepository.findByMode(mode)
                .forEach(pile -> refreshPileQueueNumbers(pile.getPileId()));
    }

    /**
     * 根据桩上车辆状态推导充电桩工作状态。
     * 仅剩排队车辆、无人充电或等待拔枪时，桩应回到 IDLE，允许下一位插入充电头。
     */
    public void updatePileWorkingState(ChargingPile pile) {
        if (chargingRequestRepository.countByPileIdAndCarStateAndActiveTrue(
                pile.getPileId(), CarState.PENDING_UNPLUG) > 0) {
            pile.setWorkingState(PileWorkingState.WAITING_UNPLUG);
        } else if (chargingRequestRepository.countByPileIdAndCarStateAndActiveTrue(
                pile.getPileId(), CarState.CHARGING) > 0) {
            pile.setWorkingState(PileWorkingState.CHARGING);
        } else if (pile.getWorkingState() != PileWorkingState.OFF
                && pile.getWorkingState() != PileWorkingState.FAULT) {
            pile.setWorkingState(PileWorkingState.IDLE);
        }
    }

    /** 充电桩故障：中断中的车辆退回等候区并触发再调度 */
    @Transactional
    public void handlePileFault(String pileId) {
        ChargingPile pile = chargingPileRepository.findById(pileId)
                .orElseThrow(() -> new IllegalArgumentException("充电桩不存在: " + pileId));
        pile.setWorkingState(PileWorkingState.FAULT);

        List<ChargingRequest> affectedCars = chargingRequestRepository
                .findByPileIdAndActiveTrueAndCarStateIn(pileId,
                        Set.of(CarState.CHARGING, CarState.PENDING_UNPLUG));
        for (ChargingRequest request : affectedCars) {
            request.setCarState(CarState.QUEUED);
            request.setPileId(null);
            request.setQueueNum(null);
            request.setBillId(null);
            pile.setOccupiedSpots(Math.max(0, pile.getOccupiedSpots() - 1));
        }
        chargingPileRepository.save(pile);

        dispatchWaitingCars(pile.getMode());
        redistributeFaultRecovery(affectedCars, pile.getMode());
    }

    /** 故障恢复后重新参与调度 */
    @Transactional
    public void recoverPileFault(String pileId) {
        ChargingPile pile = chargingPileRepository.findById(pileId)
                .orElseThrow(() -> new IllegalArgumentException("充电桩不存在: " + pileId));
        if (pile.getWorkingState() != PileWorkingState.FAULT) {
            return;
        }
        pile.setWorkingState(PileWorkingState.IDLE);
        chargingPileRepository.save(pile);
        dispatchWaitingCars(pile.getMode());
    }

    /**
     * 选取有空余等候车位的充电桩，优先选择当前排队最少的桩。
     * 用于时间顺序与优先级策略。
     */
    public Optional<ChargingPile> findAvailablePile(ChargingMode mode) {
        return chargingPileRepository.findByMode(mode).stream()
                .filter(this::hasParkingSpot)
                .min(Comparator.comparingInt(ChargingPile::getOccupiedSpots));
    }

    /** 按当前调度策略为单辆车选择充电桩 */
    private Optional<ChargingPile> selectPileForDispatch(ChargingMode mode, ChargingRequest request) {
        if (getSchedulingStrategy() == SchedulingStrategy.SHORTEST_TIME) {
            return findBestPileForShortestTime(mode, request);
        }
        return findAvailablePile(mode);
    }

    /**
     * 最短完成时间策略：选择使「等待时长 + 自身充电时长」最小的充电桩。
     */
    private Optional<ChargingPile> findBestPileForShortestTime(ChargingMode mode, ChargingRequest request) {
        return chargingPileRepository.findByMode(mode).stream()
                .filter(this::hasParkingSpot)
                .min(Comparator.comparingDouble((ChargingPile pile) ->
                                estimateTotalCompletionSeconds(pile, request))
                        .thenComparingInt(ChargingPile::getOccupiedSpots));
    }

    private boolean hasParkingSpot(ChargingPile pile) {
        return (pile.getWorkingState() == PileWorkingState.IDLE
                || pile.getWorkingState() == PileWorkingState.CHARGING
                || pile.getWorkingState() == PileWorkingState.WAITING_UNPLUG)
                && pile.getOccupiedSpots() < pile.getParkingSpots();
    }

    /**
     * 估算车辆分配到该桩后的总完成时间（秒）= 预计等待时间 + 自身充电时间。
     */
    private double estimateTotalCompletionSeconds(ChargingPile pile, ChargingRequest incoming) {
        return estimateWaitBeforeChargeSeconds(pile) + estimateChargeSeconds(incoming, pile);
    }

    /**
     * 估算新车辆在桩上开始充电前需要等待的时间（秒）。
     * 包含：当前充电剩余时间、排队车辆充电时间、等待拔枪阻塞时间。
     */
    private double estimateWaitBeforeChargeSeconds(ChargingPile pile) {
        double waitSeconds = 0.0;
        List<ChargingRequest> onPile = chargingRequestRepository
                .findByPileIdAndActiveTrueAndCarStateIn(pile.getPileId(),
                        Set.of(CarState.CHARGING, CarState.PENDING_UNPLUG, CarState.QUEUED));

        for (ChargingRequest request : onPile) {
            if (request.getCarState() == CarState.CHARGING) {
                waitSeconds += estimateRemainingChargeSeconds(request, pile);
            } else if (request.getCarState() == CarState.PENDING_UNPLUG) {
                waitSeconds += UNPLUG_BLOCK_ESTIMATE_SECONDS;
            } else if (request.getCarState() == CarState.QUEUED) {
                waitSeconds += estimateChargeSeconds(request, pile);
            }
        }
        return waitSeconds;
    }

    /** 估算车辆充满申请电量所需的充电时间（秒） */
    private double estimateChargeSeconds(ChargingRequest request, ChargingPile pile) {
        return estimateChargeSeconds(request.getRequestAmount(), pile.getChargingPower());
    }

    private double estimateChargeSeconds(double requestAmount, double powerKw) {
        if (powerKw <= 0) {
            return Double.MAX_VALUE;
        }
        return requestAmount / powerKw * 3600.0;
    }

    /** 估算充电中车辆剩余充电时间（秒） */
    private double estimateRemainingChargeSeconds(ChargingRequest request, ChargingPile pile) {
        double chargedAmount = request.getChargedAmount() != null ? request.getChargedAmount() : 0.0;
        double remainAmount = request.getRequestAmount() - chargedAmount;
        if (remainAmount <= 0) {
            return 0.0;
        }
        if (chargedAmount > 0) {
            return estimateChargeSeconds(remainAmount, pile.getChargingPower());
        }
        if (request.getStartTime() != null) {
            long elapsed = ChronoUnit.SECONDS.between(request.getStartTime(), LocalDateTime.now());
            double chargedByTime = pile.getChargingPower() * elapsed / 3600.0;
            remainAmount = Math.max(0.0, request.getRequestAmount() - chargedByTime);
        }
        return estimateChargeSeconds(remainAmount, pile.getChargingPower());
    }

    /** 按当前调度策略获取有序的等候区车辆列表 */
    private List<ChargingRequest> getOrderedWaitingCars(ChargingMode mode) {
        SchedulingStrategy strategy = getSchedulingStrategy();
        if (strategy == SchedulingStrategy.PRIORITY) {
            return chargingRequestRepository
                    .findByRequestModeAndCarStateAndActiveTrueOrderByPriorityDescRequestTimeAsc(
                            mode, CarState.WAITING);
        }
        if (strategy == SchedulingStrategy.SHORTEST_TIME) {
            return chargingRequestRepository
                    .findByRequestModeAndCarStateAndActiveTrueOrderByRequestTimeAsc(mode, CarState.WAITING)
                    .stream()
                    .sorted(Comparator.comparing(ChargingRequest::getRequestAmount)
                            .thenComparing(ChargingRequest::getRequestTime))
                    .toList();
        }
        return chargingRequestRepository
                .findByRequestModeAndCarStateAndActiveTrueOrderByRequestTimeAsc(mode, CarState.WAITING);
    }

    /** 将车辆分配到指定充电桩的排队队列 */
    private void assignCarToPile(ChargingRequest request, ChargingPile pile) {
        request.setCarState(CarState.QUEUED);
        request.setPileId(pile.getPileId());
        request.setCarPosition(0);
        int queueNum = chargingRequestRepository
                .findByPileIdAndCarStateAndActiveTrue(pile.getPileId(), CarState.QUEUED).size() + 1;
        request.setQueueNum(queueNum);
        pile.setOccupiedSpots(pile.getOccupiedSpots() + 1);
        chargingRequestRepository.save(request);
        chargingPileRepository.save(pile);
    }

    /** 故障恢复时，按策略将中断车辆重新放回等候区并逐辆再调度 */
    private void redistributeFaultRecovery(List<ChargingRequest> interruptedCars, ChargingMode mode) {
        List<ChargingRequest> ordered = interruptedCars.stream()
                .sorted(buildWaitingCarComparator())
                .toList();

        for (ChargingRequest request : ordered) {
            request.setCarState(CarState.WAITING);
            chargingRequestRepository.save(request);
            dispatchWaitingCars(mode);
        }
    }

    private Comparator<ChargingRequest> buildWaitingCarComparator() {
        SchedulingStrategy strategy = getSchedulingStrategy();
        if (strategy == SchedulingStrategy.PRIORITY) {
            return Comparator.comparing(ChargingRequest::getPriority).reversed()
                    .thenComparing(ChargingRequest::getRequestTime);
        }
        if (strategy == SchedulingStrategy.SHORTEST_TIME) {
            return Comparator.comparing(ChargingRequest::getRequestAmount)
                    .thenComparing(ChargingRequest::getRequestTime);
        }
        return Comparator.comparing(ChargingRequest::getRequestTime);
    }

    public SchedulingStrategy getSchedulingStrategy() {
        return systemConfigRepository.findAll().stream()
                .findFirst()
                .map(SystemConfig::getSchedulingStrategy)
                .orElse(SchedulingStrategy.TIME_ORDER);
    }

    @Transactional
    public void updateSchedulingStrategy(SchedulingStrategy strategy) {
        SystemConfig config = systemConfigRepository.findAll().stream()
                .findFirst()
                .orElseGet(SystemConfig::new);
        config.setSchedulingStrategy(strategy);
        systemConfigRepository.save(config);
    }
}
