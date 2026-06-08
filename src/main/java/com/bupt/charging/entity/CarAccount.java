package com.bupt.charging.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "car_account")
public class CarAccount {

    @Id
    @Column(name = "car_id", length = 32)
    private String carId;

    @Column(nullable = false, length = 64)
    private String userName;

    @Column(name = "car_capacity", nullable = false)
    private Double carCapacity;

    @Column(length = 128)
    private String password;

    @Column(nullable = false)
    private boolean registered = false;

    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Double getCarCapacity() { return carCapacity; }
    public void setCarCapacity(Double carCapacity) { this.carCapacity = carCapacity; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isRegistered() { return registered; }
    public void setRegistered(boolean registered) { this.registered = registered; }
}
