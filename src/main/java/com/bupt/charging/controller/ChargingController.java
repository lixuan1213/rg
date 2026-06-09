package com.bupt.charging.controller;

import com.bupt.charging.dto.ApiResponse;
import com.bupt.charging.dto.request.*;
import com.bupt.charging.dto.response.*;
import com.bupt.charging.enums.ChargingMode;
import com.bupt.charging.service.ChargingRequestService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户端充电相关 REST 接口。
 * <p>
 * 开始/结束充电分别对应用户手动插入/拔掉充电头。
 */
@RestController
@RequestMapping("/api/charging")
public class ChargingController {

    private final ChargingRequestService chargingRequestService;

    public ChargingController(ChargingRequestService chargingRequestService) {
        this.chargingRequestService = chargingRequestService;
    }

    /** 提交充电申请，车辆进入等候区 */
    @PostMapping("/request")
    public ApiResponse<ChargingRequestResponse> submitChargingRequest(
            @Valid @RequestBody ChargingRequestDto request) {
        return ApiResponse.success(chargingRequestService.submitChargingRequest(request));
    }

    /** 修改申请电量（充电开始前） */
    @PutMapping("/amount")
    public ApiResponse<ResultResponse> modifyAmount(@Valid @RequestBody ModifyAmountRequest request) {
        return ApiResponse.ofResult(chargingRequestService.modifyAmount(request));
    }

    /** 修改充电模式（快充/慢充），会重新进入等候区排队 */
    @PutMapping("/mode")
    public ApiResponse<ResultResponse> modifyMode(@Valid @RequestBody ModifyModeRequest request) {
        return ApiResponse.ofResult(chargingRequestService.modifyMode(request));
    }

    /** 查询排队/调度状态：前面几辆车、分配桩号、队列位置 */
    @GetMapping("/car-state")
    public ApiResponse<CarStateResponse> queryCarState(@RequestParam("carId") String carId) {
        return ApiResponse.success(chargingRequestService.queryCarState(carId));
    }

    /** 用户插入充电头，开始充电 */
    @PostMapping("/start")
    public ApiResponse<ResultResponse> startCharging(@Valid @RequestBody StartChargingRequest request) {
        return ApiResponse.ofResult(chargingRequestService.startCharging(request));
    }

    /** 查询充电进度：已充电量、剩余时间、拔枪提示等 */
    @GetMapping("/state")
    public ApiResponse<ChargingStateResponse> queryChargingState(@RequestParam("carId") String carId) {
        return ApiResponse.success(chargingRequestService.queryChargingState(carId));
    }

    /** 用户拔掉充电头，释放车位 */
    @PostMapping("/end")
    public ApiResponse<EndChargingResponse> endCharging(@Valid @RequestBody EndChargingRequest request) {
        return ApiResponse.ofEndCharging(chargingRequestService.endCharging(request));
    }

    /** 按日期查询账单列表 */
    @GetMapping("/bill")
    public ApiResponse<List<BillResponse>> requestBill(
            @RequestParam("carId") String carId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(chargingRequestService.requestBill(carId, date));
    }

    /** 查询账单详单 */
    @GetMapping("/bill/{billId}")
    public ApiResponse<BillDetailResponse> requestDetailedList(@PathVariable("billId") Long billId) {
        return ApiResponse.success(chargingRequestService.requestDetailedList(billId));
    }

    /** 查询指定模式（快充/慢充）下全部排队车辆 */
    @GetMapping("/queue")
    public ApiResponse<List<QueueStateResponse>> queryQueueState(@RequestParam("mode") ChargingMode mode) {
        return ApiResponse.success(chargingRequestService.queryQueueState(mode));
    }
}
