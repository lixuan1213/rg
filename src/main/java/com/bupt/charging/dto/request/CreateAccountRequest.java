package com.bupt.charging.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateAccountRequest {

    @NotBlank
    private String carId;

    @NotBlank
    private String userName;

    @NotNull
    @Positive
    private Double carCapacity;

    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Double getCarCapacity() { return carCapacity; }
    public void setCarCapacity(Double carCapacity) { this.carCapacity = carCapacity; }
}
