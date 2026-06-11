package com.bupt.charging.controller;

import com.bupt.charging.dto.ApiResponse;
import com.bupt.charging.dto.request.CreateAccountRequest;
import com.bupt.charging.dto.request.LoginRequest;
import com.bupt.charging.dto.request.SetPasswordRequest;
import com.bupt.charging.dto.response.AccountSummaryResponse;
import com.bupt.charging.dto.response.LoginResponse;
import com.bupt.charging.dto.response.ResultResponse;
import com.bupt.charging.service.AccountService;
import com.bupt.charging.service.AdminAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;
    private final AdminAuthService adminAuthService;

    public AccountController(AccountService accountService, AdminAuthService adminAuthService) {
        this.accountService = accountService;
        this.adminAuthService = adminAuthService;
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

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return accountService.login(request)
                .map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.fail("用户名或密码错误"));
    }

    @PostMapping("/adminlogin")
    public ApiResponse<ResultResponse> adminLogin(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ofResult(adminAuthService.login(request));
    }

    @GetMapping("/getaccounts")
    public ApiResponse<List<AccountSummaryResponse>> getAccounts() {
        return ApiResponse.success(accountService.listAllAccounts());
    }
}
