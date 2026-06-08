package com.bupt.charging.service;

import com.bupt.charging.dto.request.CreateAccountRequest;
import com.bupt.charging.dto.request.SetPasswordRequest;
import com.bupt.charging.entity.CarAccount;
import com.bupt.charging.repository.CarAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final CarAccountRepository carAccountRepository;

    public AccountService(CarAccountRepository carAccountRepository) {
        this.carAccountRepository = carAccountRepository;
    }

    @Transactional
    public boolean createNewAccount(CreateAccountRequest request) {
        if (carAccountRepository.existsById(request.getCarId())) {
            return false;
        }
        CarAccount account = new CarAccount();
        account.setCarId(request.getCarId());
        account.setUserName(request.getUserName());
        account.setCarCapacity(request.getCarCapacity());
        carAccountRepository.save(account);
        return true;
    }

    @Transactional
    public boolean setPassword(SetPasswordRequest request) {
        return carAccountRepository.findById(request.getCarId())
                .map(account -> {
                    account.setPassword(request.getPassword());
                    account.setRegistered(true);
                    carAccountRepository.save(account);
                    return true;
                })
                .orElse(false);
    }

    public CarAccount getAccount(String carId) {
        return carAccountRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("车辆账号不存在: " + carId));
    }
}
