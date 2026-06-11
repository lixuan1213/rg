package com.bupt.charging.service;

import com.bupt.charging.config.AdminProperties;
import com.bupt.charging.dto.request.LoginRequest;
import com.bupt.charging.dto.response.ResultResponse;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AdminAuthService {

    private final AdminProperties adminProperties;

    public AdminAuthService(AdminProperties adminProperties) {
        this.adminProperties = adminProperties;
    }

    public ResultResponse login(LoginRequest request) {
        if (Objects.equals(adminProperties.getUsername(), request.getUserName())
                && Objects.equals(adminProperties.getPassword(), request.getPassword())) {
            return ResultResponse.success();
        }
        return ResultResponse.fail("管理员用户名或密码错误");
    }
}
