package com.bupt.charging.controller;

import com.bupt.charging.dto.ApiResponse;
import com.bupt.charging.dto.request.CreateAccountRequest;
import com.bupt.charging.dto.request.SetPasswordRequest;
import com.bupt.charging.dto.response.ResultResponse;
import com.bupt.charging.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/create")
    public ApiResponse<ResultResponse> createNewAccount(@Valid @RequestBody CreateAccountRequest request) {
        boolean success = accountService.createNewAccount(request);
        return ApiResponse.ofResult(success ? ResultResponse.success() : ResultResponse.fail("账号已存在"));
    }

    @PostMapping("/set-pwd")
    public ApiResponse<ResultResponse> setPassword(@Valid @RequestBody SetPasswordRequest request) {
        boolean success = accountService.setPassword(request);
        return ApiResponse.ofResult(success ? ResultResponse.success() : ResultResponse.fail("车辆账号不存在"));
    }
}
