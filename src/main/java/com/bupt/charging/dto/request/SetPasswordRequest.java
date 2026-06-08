package com.bupt.charging.dto.request;

import jakarta.validation.constraints.NotBlank;

public class SetPasswordRequest {

    @NotBlank
    private String carId;

    @NotBlank
    private String password;

    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
