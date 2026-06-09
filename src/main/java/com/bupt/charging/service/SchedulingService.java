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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class SchedulingService {

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

    @Transactional
    public void dispatchWaitingCars(ChargingMode mode) {
        syncPileOccupiedSpots(mode);
        List<ChargingRequest> waitingCars = getOrderedWaitingCars(mode);
        for (ChargingRequest request : waitingCars) {
            Optional<ChargingPile> pileOpt = findAvailablePile(mode);
            if (pileOpt.isEmpty()) {
                break;
            }
            assignCarToPile(request, pileOpt.get());
        }
        refreshAllPileQueueNumbers(mode);
    }

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

    public void refreshPileQueueNumbers(String pileId) {
        List<ChargingRequest> queued = chargingRequestRepository
                .findByPileIdAndCarStateAndActiveTrueOrderByQueueNumAsc(pileId, CarState.QUEUED);
        for (int i = 0; i < queued.size(); i++) {
            queued.get(i).setQueueNum(i + 1);
            chargingRequestRepository.save(queued.get(i));
        }
    }

    public void refreshAllPileQueueNumbers(ChargingMode mode) {
        chargingPileRepository.findByMode(mode)
                .forEach(pile -> refreshPileQueueNumbers(pile.getPileId()));
    }

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

    public Optional<ChargingPile> findAvailablePile(ChargingMode mode) {
        return chargingPileRepository.findByMode(mode).stream()
                .filter(pile -> pile.getWorkingState() == PileWorkingState.IDLE
                        || pile.getWorkingState() == PileWorkingState.CHARGING
                        || pile.getWorkingState() == PileWorkingState.WAITING_UNPLUG)
                .filter(pile -> pile.getOccupiedSpots() < pile.getParkingSpots())
                .min(Comparator.comparingInt(ChargingPile::getOccupiedSpots));
    }

    private List<ChargingRequest> getOrderedWaitingCars(ChargingMode mode) {
        if (getSchedulingStrategy() == SchedulingStrategy.PRIORITY) {
            return chargingRequestRepository
                    .findByRequestModeAndCarStateAndActiveTrueOrderByPriorityDescRequestTimeAsc(
                            mode, CarState.WAITING);
        }
        return chargingRequestRepository
                .findByRequestModeAndCarStateAndActiveTrueOrderByRequestTimeAsc(mode, CarState.WAITING);
    }

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

    private void redistributeFaultRecovery(List<ChargingRequest> interruptedCars, ChargingMode mode) {
        SchedulingStrategy strategy = getSchedulingStrategy();
        List<ChargingRequest> ordered = interruptedCars.stream()
                .sorted(strategy == SchedulingStrategy.PRIORITY
                        ? Comparator.comparing(ChargingRequest::getPriority).reversed()
                                .thenComparing(ChargingRequest::getRequestTime)
                        : Comparator.comparing(ChargingRequest::getRequestTime))
                .toList();

        for (ChargingRequest request : ordered) {
            request.setCarState(CarState.WAITING);
            chargingRequestRepository.save(request);
            dispatchWaitingCars(mode);
        }
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
