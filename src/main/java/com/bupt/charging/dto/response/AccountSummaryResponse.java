package com.bupt.charging.dto.response;

import com.bupt.charging.entity.CarAccount;

public class AccountSummaryResponse {

    private String carId;
    private String userName;
    private Double carCapacity;
    private boolean registered;

    public static AccountSummaryResponse from(CarAccount account) {
        AccountSummaryResponse response = new AccountSummaryResponse();
        response.setCarId(account.getCarId());
        response.setUserName(account.getUserName());
        response.setCarCapacity(account.getCarCapacity());
        response.setRegistered(account.isRegistered());
        return response;
    }

    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Double getCarCapacity() { return carCapacity; }
    public void setCarCapacity(Double carCapacity) { this.carCapacity = carCapacity; }
    public boolean isRegistered() { return registered; }
    public void setRegistered(boolean registered) { this.registered = registered; }
}
