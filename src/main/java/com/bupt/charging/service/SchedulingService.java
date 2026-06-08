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
        List<ChargingRequest> waitingCars = getOrderedWaitingCars(mode);
        for (ChargingRequest request : waitingCars) {
            Optional<ChargingPile> pileOpt = findAvailablePile(mode);
            if (pileOpt.isEmpty()) {
                break;
            }
            assignCarToPile(request, pileOpt.get());
        }
    }

    @Transactional
    public void handlePileFault(String pileId) {
        ChargingPile pile = chargingPileRepository.findById(pileId)
                .orElseThrow(() -> new IllegalArgumentException("充电桩不存在: " + pileId));
        pile.setWorkingState(PileWorkingState.FAULT);

        List<ChargingRequest> chargingCars = chargingRequestRepository
                .findByPileIdAndCarStateAndActiveTrue(pileId, CarState.CHARGING);
        for (ChargingRequest request : chargingCars) {
            request.setCarState(CarState.QUEUED);
            request.setPileId(null);
            pile.setOccupiedSpots(Math.max(0, pile.getOccupiedSpots() - 1));
        }
        chargingPileRepository.save(pile);

        dispatchWaitingCars(pile.getMode());
        redistributeFaultRecovery(chargingCars, pile.getMode());
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
                        || pile.getWorkingState() == PileWorkingState.CHARGING)
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
