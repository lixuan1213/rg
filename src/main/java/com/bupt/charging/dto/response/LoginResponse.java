package com.bupt.charging.dto.response;

public class LoginResponse {

    private String carId;
    private String userName;
    private Double carCapacity;

    public LoginResponse() {}

    public LoginResponse(String carId, String userName, Double carCapacity) {
        this.carId = carId;
        this.userName = userName;
        this.carCapacity = carCapacity;
    }

    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Double getCarCapacity() { return carCapacity; }
    public void setCarCapacity(Double carCapacity) { this.carCapacity = carCapacity; }
}
