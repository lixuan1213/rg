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

@RestController
@RequestMapping("/api/charging")
public class ChargingController {

    private final ChargingRequestService chargingRequestService;

    public ChargingController(ChargingRequestService chargingRequestService) {
        this.chargingRequestService = chargingRequestService;
    }

    @PostMapping("/request")
    public ApiResponse<ChargingRequestResponse> submitChargingRequest(
            @Valid @RequestBody ChargingRequestDto request) {
        return ApiResponse.success(chargingRequestService.submitChargingRequest(request));
    }

    @PutMapping("/amount")
    public ApiResponse<ResultResponse> modifyAmount(@Valid @RequestBody ModifyAmountRequest request) {
        return ApiResponse.success(chargingRequestService.modifyAmount(request));
    }

    @PutMapping("/mode")
    public ApiResponse<ResultResponse> modifyMode(@Valid @RequestBody ModifyModeRequest request) {
        return ApiResponse.success(chargingRequestService.modifyMode(request));
    }

    @GetMapping("/car-state/{carId}")
    public ApiResponse<CarStateResponse> queryCarState(@PathVariable String carId) {
        return ApiResponse.success(chargingRequestService.queryCarState(carId));
    }

    @PostMapping("/start")
    public ApiResponse<ResultResponse> startCharging(@Valid @RequestBody StartChargingRequest request) {
        return ApiResponse.success(chargingRequestService.startCharging(request));
    }

    @GetMapping("/state/{carId}")
    public ApiResponse<ChargingStateResponse> queryChargingState(@PathVariable String carId) {
        return ApiResponse.success(chargingRequestService.queryChargingState(carId));
    }

    @PostMapping("/end")
    public ApiResponse<ResultResponse> endCharging(@Valid @RequestBody EndChargingRequest request) {
        return ApiResponse.success(chargingRequestService.endCharging(request));
    }

    @GetMapping("/bill")
    public ApiResponse<List<BillResponse>> requestBill(
            @RequestParam String carId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(chargingRequestService.requestBill(carId, date));
    }

    @GetMapping("/bill/{billId}")
    public ApiResponse<BillDetailResponse> requestDetailedList(@PathVariable Long billId) {
        return ApiResponse.success(chargingRequestService.requestDetailedList(billId));
    }

    @GetMapping("/queue")
    public ApiResponse<List<QueueStateResponse>> queryQueueState(@RequestParam ChargingMode mode) {
        return ApiResponse.success(chargingRequestService.queryQueueState(mode));
    }
}
