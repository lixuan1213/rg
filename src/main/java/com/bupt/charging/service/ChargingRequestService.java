package com.bupt.charging.service;

import com.bupt.charging.dto.request.*;
import com.bupt.charging.dto.response.*;
import com.bupt.charging.entity.Bill;
import com.bupt.charging.entity.CarAccount;
import com.bupt.charging.entity.ChargingPile;
import com.bupt.charging.entity.ChargingRequest;
import com.bupt.charging.enums.CarState;
import com.bupt.charging.enums.ChargingMode;
import com.bupt.charging.enums.PileWorkingState;
import com.bupt.charging.repository.ChargingRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Function;

@Service
public class ChargingRequestService {

    private final ChargingRequestRepository chargingRequestRepository;
    private final AccountService accountService;
    private final ChargingPileService chargingPileService;
    private final SchedulingService schedulingService;
    private final BillingService billingService;

    public ChargingRequestService(ChargingRequestRepository chargingRequestRepository,
                                  AccountService accountService,
                                  ChargingPileService chargingPileService,
                                  SchedulingService schedulingService,
                                  BillingService billingService) {
        this.chargingRequestRepository = chargingRequestRepository;
        this.accountService = accountService;
        this.chargingPileService = chargingPileService;
        this.schedulingService = schedulingService;
        this.billingService = billingService;
    }

    @Transactional
    public ChargingRequestResponse submitChargingRequest(ChargingRequestDto dto) {
        CarAccount account = accountService.getAccount(dto.getCarId());
        if (!account.isRegistered()) {
            throw new IllegalStateException("车辆账号未完成注册");
        }
        chargingRequestRepository.findFirstByCarIdAndActiveTrue(dto.getCarId())
                .ifPresent(req -> {
                    throw new IllegalStateException("车辆已有进行中的充电请求");
                });

        ChargingRequest request = new ChargingRequest();
        request.setCarId(dto.getCarId());
        request.setRequestAmount(dto.getRequestAmount());
        request.setRequestMode(dto.getRequestMode());
        request.setCarCapacity(account.getCarCapacity());
        request.setRequestTime(LocalDateTime.now());
        request.setCarState(CarState.WAITING);
        request.setCarPosition(0);
        chargingRequestRepository.save(request);

        schedulingService.dispatchWaitingCars(dto.getRequestMode());
        refreshQueueNumbers(dto.getRequestMode());

        ChargingRequest latest = chargingRequestRepository.findFirstByCarIdAndActiveTrue(dto.getCarId())
                .orElse(request);
        return new ChargingRequestResponse(
                latest.getCarPosition(),
                latest.getCarState(),
                latest.getQueueNum(),
                latest.getRequestTime()
        );
    }

    @Transactional
    public ResultResponse modifyAmount(ModifyAmountRequest request) {
        return updateActiveRequest(request.getCarId(), req -> {
            if (req.getCarState() == CarState.CHARGING) {
                return false;
            }
            req.setRequestAmount(request.getAmount());
            chargingRequestRepository.save(req);
            return true;
        });
    }

    @Transactional
    public ResultResponse modifyMode(ModifyModeRequest request) {
        return updateActiveRequest(request.getCarId(), req -> {
            if (req.getCarState() == CarState.CHARGING) {
                return false;
            }
            ChargingMode oldMode = req.getRequestMode();
            req.setRequestMode(request.getMode());
            req.setCarState(CarState.WAITING);
            req.setPileId(null);
            req.setQueueNum(null);
            chargingRequestRepository.save(req);
            refreshQueueNumbers(oldMode);
            schedulingService.dispatchWaitingCars(request.getMode());
            refreshQueueNumbers(request.getMode());
            return true;
        });
    }

    public CarStateResponse queryCarState(String carId) {
        ChargingRequest request = getActiveRequest(carId);
        long before = 0;
        if (request.getQueueNum() != null && request.getCarState() == CarState.QUEUED) {
            before = chargingRequestRepository.countByRequestModeAndCarStateAndActiveTrueAndQueueNumLessThan(
                    request.getRequestMode(), CarState.QUEUED, request.getQueueNum());
        }
        return new CarStateResponse(
                before,
                request.getCarState(),
                request.getQueueNum(),
                request.getRequestTime()
        );
    }

    @Transactional
    public ResultResponse startCharging(StartChargingRequest request) {
        ChargingRequest chargingRequest = getActiveRequest(request.getCarId());
        ChargingPile pile = chargingPileService.getPile(request.getChargePileNum());

        if (chargingRequest.getCarState() != CarState.QUEUED
                || !request.getChargePileNum().equals(chargingRequest.getPileId())) {
            return ResultResponse.fail();
        }
        if (pile.getWorkingState() == PileWorkingState.OFF
                || pile.getWorkingState() == PileWorkingState.FAULT) {
            return ResultResponse.fail();
        }

        chargingRequest.setCarState(CarState.CHARGING);
        chargingRequest.setStartTime(LocalDateTime.now());
        pile.setWorkingState(PileWorkingState.CHARGING);
        chargingRequestRepository.save(chargingRequest);
        chargingPileService.savePile(pile);
        return ResultResponse.success();
    }

    public ChargingStateResponse queryChargingState(String carId) {
        ChargingRequest request = getActiveRequest(carId);
        Long remaining = null;
        if (request.getCarState() == CarState.CHARGING && request.getPileId() != null) {
            ChargingPile pile = chargingPileService.getPile(request.getPileId());
            double remainAmount = request.getRequestAmount() - request.getChargedAmount();
            remaining = (long) Math.ceil(remainAmount / pile.getChargingPower() * 60);
        }
        return new ChargingStateResponse(
                request.getCarId(),
                request.getPileId(),
                request.getRequestMode(),
                request.getRequestAmount(),
                request.getChargedAmount(),
                request.getCarState(),
                request.getStartTime(),
                remaining
        );
    }

    @Transactional
    public ResultResponse endCharging(EndChargingRequest request) {
        ChargingRequest chargingRequest = getActiveRequest(request.getCarId());
        if (chargingRequest.getCarState() != CarState.CHARGING
                || !request.getChargingPileNum().equals(chargingRequest.getPileId())) {
            return ResultResponse.fail();
        }

        ChargingPile pile = chargingPileService.getPile(request.getChargingPileNum());
        LocalDateTime endTime = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(chargingRequest.getStartTime(), endTime);
        if (minutes <= 0) {
            minutes = 1;
        }
        double chargedAmount = pile.getChargingPower() * minutes / 60.0;
        chargingRequest.setChargedAmount(chargedAmount);
        chargingRequest.setEndTime(endTime);
        chargingRequest.setCarState(CarState.COMPLETED);
        chargingRequest.setActive(false);

        pile.setTotalChargeNum(pile.getTotalChargeNum() + 1);
        pile.setTotalChargeTime(pile.getTotalChargeTime() + minutes);
        pile.setTotalCapacity(pile.getTotalCapacity() + chargedAmount);
        pile.setOccupiedSpots(Math.max(0, pile.getOccupiedSpots() - 1));
        if (pile.getOccupiedSpots() == 0) {
            pile.setWorkingState(PileWorkingState.IDLE);
        }

        billingService.createBill(chargingRequest, pile);
        chargingRequestRepository.save(chargingRequest);
        chargingPileService.savePile(pile);
        schedulingService.dispatchWaitingCars(pile.getMode());
        return ResultResponse.success();
    }

    public List<BillResponse> requestBill(String carId, LocalDate date) {
        return billingService.getBills(carId, date).stream()
                .map(this::toBillResponse)
                .toList();
    }

    public BillDetailResponse requestDetailedList(Long billId) {
        Bill bill = billingService.getBill(billId);
        return new BillDetailResponse(
                bill.getCarId(),
                bill.getDate(),
                bill.getBillId(),
                bill.getChargePileNum(),
                bill.getChargeAmount(),
                bill.getChargeDuration(),
                bill.getStartTime(),
                bill.getEndTime(),
                bill.getChargeFee(),
                bill.getServiceFee(),
                bill.getTotalFee()
        );
    }

    public List<QueueStateResponse> queryQueueState(ChargingMode mode) {
        return chargingRequestRepository
                .findByRequestModeAndCarStateAndActiveTrueOrderByRequestTimeAsc(mode, CarState.QUEUED)
                .stream()
                .map(req -> new QueueStateResponse(
                        req.getCarId(),
                        req.getCarCapacity(),
                        req.getRequestAmount(),
                        ChronoUnit.MINUTES.between(req.getRequestTime(), LocalDateTime.now())
                ))
                .toList();
    }

    private ResultResponse updateActiveRequest(String carId, Function<ChargingRequest, Boolean> updater) {
        try {
            ChargingRequest request = getActiveRequest(carId);
            return updater.apply(request) ? ResultResponse.success() : ResultResponse.fail();
        } catch (IllegalArgumentException ex) {
            return ResultResponse.fail();
        }
    }

    private ChargingRequest getActiveRequest(String carId) {
        return chargingRequestRepository.findFirstByCarIdAndActiveTrue(carId)
                .orElseThrow(() -> new IllegalArgumentException("未找到进行中的充电请求: " + carId));
    }

    private void refreshQueueNumbers(ChargingMode mode) {
        List<ChargingRequest> waiting = chargingRequestRepository
                .findByRequestModeAndCarStateAndActiveTrueOrderByRequestTimeAsc(mode, CarState.WAITING);
        for (int i = 0; i < waiting.size(); i++) {
            waiting.get(i).setCarPosition(i + 1);
            chargingRequestRepository.save(waiting.get(i));
        }
    }

    private BillResponse toBillResponse(Bill bill) {
        return new BillResponse(
                bill.getCarId(),
                bill.getDate(),
                bill.getBillId(),
                bill.getChargePileNum(),
                bill.getChargeAmount(),
                bill.getChargeDuration(),
                bill.getStartTime(),
                bill.getEndTime(),
                bill.getChargeFee(),
                bill.getServiceFee(),
                bill.getTotalFee()
        );
    }
}
