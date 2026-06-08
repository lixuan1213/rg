package com.bupt.charging.controller;

import com.bupt.charging.dto.ApiResponse;
import com.bupt.charging.dto.request.SetParametersRequest;
import com.bupt.charging.dto.response.PileStateResponse;
import com.bupt.charging.dto.response.ResultResponse;
import com.bupt.charging.service.ChargingPileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pile")
public class ChargingPileController {

    private final ChargingPileService chargingPileService;

    public ChargingPileController(ChargingPileService chargingPileService) {
        this.chargingPileService = chargingPileService;
    }

    @PostMapping("/{pileId}/power-on")
    public ApiResponse<ResultResponse> powerOn(@PathVariable String pileId) {
        return ApiResponse.success(chargingPileService.powerOn(pileId));
    }

    @PostMapping("/{pileId}/power-off")
    public ApiResponse<ResultResponse> powerOff(@PathVariable String pileId) {
        return ApiResponse.success(chargingPileService.powerOff(pileId));
    }

    @PostMapping("/{pileId}/start")
    public ApiResponse<ResultResponse> startChargingPile(@PathVariable String pileId) {
        return ApiResponse.success(chargingPileService.startChargingPile(pileId));
    }

    @PutMapping("/parameters")
    public ApiResponse<ResultResponse> setParameters(@Valid @RequestBody SetParametersRequest request) {
        return ApiResponse.success(chargingPileService.setParameters(request));
    }

    @GetMapping("/{pileId}/state")
    public ApiResponse<PileStateResponse> queryPileState(@PathVariable String pileId) {
        return ApiResponse.success(chargingPileService.queryPileState(pileId));
    }

    @GetMapping("/state/all")
    public ApiResponse<List<PileStateResponse>> queryAllPileStates() {
        return ApiResponse.success(chargingPileService.queryAllPileStates());
    }

    @PostMapping("/{pileId}/fault")
    public ApiResponse<ResultResponse> reportFault(@PathVariable String pileId) {
        return ApiResponse.success(chargingPileService.reportFault(pileId));
    }

    @PostMapping("/{pileId}/recover")
    public ApiResponse<ResultResponse> recoverFault(@PathVariable String pileId) {
        return ApiResponse.success(chargingPileService.recoverFault(pileId));
    }
}
