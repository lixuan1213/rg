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
import com.bupt.charging.enums.SchedulingStrategy;
import com.bupt.charging.repository.ChargingRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Service
public class ChargingRequestService {

    private static final String UNPLUG_REMINDER = "充电已完成，请拔掉充电头";
    private static final Set<CarState> PILE_BLOCKING_STATES = Set.of(CarState.CHARGING, CarState.PENDING_UNPLUG);

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
        validateRequestAmount(account, dto.getRequestAmount());

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
                latest.getPileId(),
                latest.getQueueNum(),
                latest.getRequestTime()
        );
    }

    @Transactional
    public ResultResponse modifyAmount(ModifyAmountRequest request) {
        CarAccount account = accountService.getAccount(request.getCarId());
        validateRequestAmount(account, request.getAmount());
        return updateActiveRequest(request.getCarId(), req -> {
            if (isChargingInProgress(req)) {
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
            if (isChargingInProgress(req)) {
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
        if (request.getPileId() != null) {
            schedulingService.syncPileOccupiedSpots(request.getRequestMode());
        }
        long before = calculateCarsBefore(request);
        return new CarStateResponse(
                before,
                request.getCarState(),
                request.getPileId(),
                request.getQueueNum(),
                request.getRequestTime(),
                buildReminderMessage(request)
        );
    }

    @Transactional
    public ResultResponse startCharging(StartChargingRequest request) {
        ChargingRequest chargingRequest = getActiveRequest(request.getCarId());

        if (chargingRequest.getCarState() != CarState.QUEUED) {
            return ResultResponse.fail("当前状态不允许插入充电头，请等待调度分配到充电桩");
        }
        String assignedPileId = chargingRequest.getPileId();
        if (assignedPileId == null) {
            return ResultResponse.fail("尚未分配到充电桩，请等待调度");
        }
        if (request.getChargePileNum() != null
                && !assignedPileId.equals(request.getChargePileNum())) {
            return ResultResponse.fail("充电桩不匹配，请使用系统分配的充电桩: " + assignedPileId);
        }

        schedulingService.syncPileOccupiedSpots(chargingRequest.getRequestMode());
        ChargingPile pile = chargingPileService.getPile(assignedPileId);
        if (pile.getMode() != chargingRequest.getRequestMode()) {
            return ResultResponse.fail("充电桩类型与充电模式不匹配");
        }
        if (pile.getWorkingState() == PileWorkingState.OFF
                || pile.getWorkingState() == PileWorkingState.FAULT) {
            return ResultResponse.fail("充电桩当前不可用");
        }
        if (pile.getWorkingState() == PileWorkingState.WAITING_UNPLUG) {
            return ResultResponse.fail("充电桩等待上一用户拔下充电头，请稍后再插入");
        }
        if (chargingRequest.getQueueNum() == null || chargingRequest.getQueueNum() != 1) {
            return ResultResponse.fail("请等待排到该充电桩队列第一位后再插入充电头");
        }
        if (isPilePhysicallyBlocked(assignedPileId)) {
            return ResultResponse.fail("该充电桩正在服务其他车辆或等待拔枪，请排队等待");
        }

        chargingRequest.setCarState(CarState.CHARGING);
        chargingRequest.setStartTime(LocalDateTime.now());
        pile.setWorkingState(PileWorkingState.CHARGING);
        chargingRequestRepository.save(chargingRequest);
        chargingPileService.savePile(pile);
        schedulingService.refreshPileQueueNumbers(assignedPileId);
        return ResultResponse.success("充电头已插入，开始充电");
    }

    @Transactional
    public ChargingStateResponse queryChargingState(String carId) {
        ChargingRequest request = getActiveRequest(carId);
        if (request.getCarState() == CarState.CHARGING) {
            tryAutoCompleteIfFull(request);
            request = getActiveRequest(carId);
        }
        return buildChargingStateResponse(request);
    }

    @Transactional
    public void refreshChargingProgress() {
        List<ChargingRequest> chargingList =
                chargingRequestRepository.findByCarStateAndActiveTrue(CarState.CHARGING);
        for (ChargingRequest request : chargingList) {
            tryAutoCompleteIfFull(request);
        }
    }

    @Transactional
    public EndChargingResponse endCharging(EndChargingRequest request) {
        ChargingRequest chargingRequest;
        try {
            chargingRequest = getActiveRequest(request.getCarId());
        } catch (IllegalArgumentException ex) {
            return EndChargingResponse.fail("未找到进行中的充电请求");
        }

        String assignedPileId = chargingRequest.getPileId();
        if (assignedPileId == null) {
            return EndChargingResponse.fail("未找到当前充电的充电桩");
        }
        if (request.getChargePileNum() != null
                && !assignedPileId.equals(request.getChargePileNum())) {
            return EndChargingResponse.fail("充电桩不匹配，当前充电桩为: " + assignedPileId);
        }

        ChargingPile pile = chargingPileService.getPile(assignedPileId);

        if (chargingRequest.getCarState() == CarState.PENDING_UNPLUG) {
            Bill bill = releaseAfterUnplug(chargingRequest, pile);
            return EndChargingResponse.success(
                    "充电头已拔下，车位已释放，下一位用户可插入充电头",
                    bill.getBillId(),
                    bill.getChargePileNum(),
                    bill.getChargeAmount(),
                    bill.getChargeDuration(),
                    bill.getTotalFee()
            );
        }

        if (chargingRequest.getCarState() != CarState.CHARGING) {
            return EndChargingResponse.fail("当前未插入充电头，无法执行拔枪操作");
        }

        LocalDateTime endTime = LocalDateTime.now();
        double chargedAmount = calculateChargedAmount(chargingRequest, pile, endTime);
        if (chargedAmount <= 0) {
            chargedAmount = pile.getChargingPower() / 60.0;
        }
        Bill bill = finalizeAndRelease(chargingRequest, pile, endTime, chargedAmount);
        return EndChargingResponse.success(
                "充电头已拔下，充电结束，账单已生成",
                bill.getBillId(),
                bill.getChargePileNum(),
                bill.getChargeAmount(),
                bill.getChargeDuration(),
                bill.getTotalFee()
        );
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
        schedulingService.dispatchWaitingCars(mode);
        refreshQueueNumbers(mode);
        List<ChargingRequest> requests = chargingRequestRepository.findByRequestModeAndActiveTrueAndCarStateIn(
                mode, List.of(CarState.WAITING, CarState.QUEUED, CarState.CHARGING, CarState.PENDING_UNPLUG));
        Comparator<ChargingRequest> order = schedulingService.getSchedulingStrategy() == SchedulingStrategy.PRIORITY
                ? Comparator.comparing(ChargingRequest::getPriority).reversed()
                        .thenComparing(ChargingRequest::getRequestTime)
                : Comparator.comparing(ChargingRequest::getRequestTime);
        LocalDateTime now = LocalDateTime.now();
        return requests.stream()
                .sorted(order)
                .map(req -> new QueueStateResponse(
                        req.getCarId(),
                        req.getCarCapacity(),
                        req.getRequestAmount(),
                        ChronoUnit.MINUTES.between(req.getRequestTime(), now)
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

    private long calculateCarsBefore(ChargingRequest request) {
        if (request.getCarState() == CarState.WAITING) {
            return request.getCarPosition() != null && request.getCarPosition() > 1
                    ? request.getCarPosition() - 1L : 0L;
        }
        if (request.getCarState() == CarState.QUEUED && request.getQueueNum() != null) {
            long before = request.getQueueNum() - 1L;
            if (request.getPileId() != null) {
                before += chargingRequestRepository.countByPileIdAndActiveTrueAndCarStateIn(
                        request.getPileId(), PILE_BLOCKING_STATES);
            }
            return before;
        }
        return 0L;
    }

    private boolean isChargingInProgress(ChargingRequest request) {
        return request.getCarState() == CarState.CHARGING
                || request.getCarState() == CarState.PENDING_UNPLUG;
    }

    private boolean isPilePhysicallyBlocked(String pileId) {
        return chargingRequestRepository.countByPileIdAndActiveTrueAndCarStateIn(
                pileId, PILE_BLOCKING_STATES) > 0;
    }

    private String buildReminderMessage(ChargingRequest request) {
        if (request.getCarState() == CarState.PENDING_UNPLUG) {
            return UNPLUG_REMINDER;
        }
        if (request.getCarState() == CarState.QUEUED
                && request.getPileId() != null
                && isPilePhysicallyBlocked(request.getPileId())) {
            return "请等待前一位用户拔掉充电头后再插入";
        }
        return null;
    }

    private ChargingStateResponse buildChargingStateResponse(ChargingRequest request) {
        double chargedAmount = request.getChargedAmount();
        Long elapsedSeconds = null;
        Long remaining = null;
        String reminder = buildReminderMessage(request);

        if (request.getCarState() == CarState.CHARGING && request.getPileId() != null) {
            ChargingPile pile = chargingPileService.getPile(request.getPileId());
            LocalDateTime now = LocalDateTime.now();
            elapsedSeconds = calculateElapsedSeconds(request, now);
            chargedAmount = calculateChargedAmount(request, pile, now);
            double remainAmount = request.getRequestAmount() - chargedAmount;
            remaining = remainAmount <= 0 ? 0L
                    : (long) Math.ceil(remainAmount / pile.getChargingPower() * 60);
        } else if (request.getCarState() == CarState.PENDING_UNPLUG) {
            elapsedSeconds = calculateElapsedSeconds(request, request.getEndTime());
            remaining = 0L;
        }

        return new ChargingStateResponse(
                request.getCarId(),
                request.getPileId(),
                request.getRequestMode(),
                request.getRequestAmount(),
                chargedAmount,
                request.getCarState(),
                request.getStartTime(),
                elapsedSeconds,
                remaining,
                reminder
        );
    }

    private void validateRequestAmount(CarAccount account, Double requestAmount) {
        if (requestAmount > account.getCarCapacity()) {
            throw new IllegalArgumentException(
                    "充电请求量(" + requestAmount + " kWh)不能超过车辆最大容量("
                            + account.getCarCapacity() + " kWh)");
        }
    }

    private long calculateElapsedSeconds(ChargingRequest request, LocalDateTime asOf) {
        if (request.getStartTime() == null || asOf == null) {
            return 0L;
        }
        return Math.max(0L, ChronoUnit.SECONDS.between(request.getStartTime(), asOf));
    }

    private double calculateChargedAmount(ChargingRequest request, ChargingPile pile, LocalDateTime asOf) {
        long seconds = calculateElapsedSeconds(request, asOf);
        if (seconds <= 0) {
            return 0.0;
        }
        double amount = pile.getChargingPower() * seconds / 3600.0;
        return round2(Math.min(amount, request.getRequestAmount()));
    }

    private long calculateDurationMinutes(ChargingRequest request, LocalDateTime endTime) {
        long seconds = calculateElapsedSeconds(request, endTime);
        return Math.max(1L, (seconds + 59) / 60);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private boolean tryAutoCompleteIfFull(ChargingRequest request) {
        if (request.getCarState() != CarState.CHARGING
                || request.getPileId() == null
                || request.getStartTime() == null) {
            return false;
        }
        ChargingPile pile = chargingPileService.getPile(request.getPileId());
        double currentAmount = calculateChargedAmount(request, pile, LocalDateTime.now());
        if (currentAmount < request.getRequestAmount()) {
            request.setChargedAmount(currentAmount);
            chargingRequestRepository.save(request);
            return false;
        }
        long neededSeconds = (long) Math.ceil(request.getRequestAmount() / pile.getChargingPower() * 3600.0);
        if (neededSeconds <= 0) {
            neededSeconds = 1;
        }
        LocalDateTime endTime = request.getStartTime().plusSeconds(neededSeconds);
        if (endTime.isAfter(LocalDateTime.now())) {
            endTime = LocalDateTime.now();
        }
        enterPendingUnplug(request, pile, endTime, request.getRequestAmount());
        return true;
    }

    private Bill enterPendingUnplug(ChargingRequest chargingRequest, ChargingPile pile,
                                    LocalDateTime endTime, double chargedAmount) {
        long minutes = calculateDurationMinutes(chargingRequest, endTime);
        chargingRequest.setChargedAmount(chargedAmount);
        chargingRequest.setEndTime(endTime);
        chargingRequest.setCarState(CarState.PENDING_UNPLUG);

        pile.setTotalChargeNum(pile.getTotalChargeNum() + 1);
        pile.setTotalChargeTime(pile.getTotalChargeTime() + minutes);
        pile.setTotalCapacity(pile.getTotalCapacity() + chargedAmount);
        pile.setWorkingState(PileWorkingState.WAITING_UNPLUG);

        Bill bill = billingService.createBill(chargingRequest, pile);
        chargingRequest.setBillId(bill.getBillId());
        chargingRequestRepository.save(chargingRequest);
        chargingPileService.savePile(pile);
        return bill;
    }

    private Bill finalizeAndRelease(ChargingRequest chargingRequest, ChargingPile pile,
                                    LocalDateTime endTime, double chargedAmount) {
        long minutes = calculateDurationMinutes(chargingRequest, endTime);
        chargingRequest.setChargedAmount(chargedAmount);
        chargingRequest.setEndTime(endTime);

        pile.setTotalChargeNum(pile.getTotalChargeNum() + 1);
        pile.setTotalChargeTime(pile.getTotalChargeTime() + minutes);
        pile.setTotalCapacity(pile.getTotalCapacity() + chargedAmount);

        Bill bill = billingService.createBill(chargingRequest, pile);
        chargingRequest.setBillId(bill.getBillId());
        return releaseAfterUnplug(chargingRequest, pile, bill);
    }

    private Bill releaseAfterUnplug(ChargingRequest chargingRequest, ChargingPile pile) {
        Bill bill = billingService.getBill(chargingRequest.getBillId());
        return releaseAfterUnplug(chargingRequest, pile, bill);
    }

    private Bill releaseAfterUnplug(ChargingRequest chargingRequest, ChargingPile pile, Bill bill) {
        chargingRequest.setCarState(CarState.COMPLETED);
        chargingRequest.setActive(false);
        chargingRequestRepository.save(chargingRequest);
        chargingRequestRepository.flush();

        schedulingService.syncPileOccupiedSpots(pile.getMode());
        schedulingService.refreshPileQueueNumbers(pile.getPileId());
        schedulingService.dispatchWaitingCars(pile.getMode());
        return bill;
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
