package com.bupt.charging.service;

import com.bupt.charging.dto.request.CreateAccountRequest;
import com.bupt.charging.dto.request.LoginRequest;
import com.bupt.charging.dto.request.SetPasswordRequest;
import com.bupt.charging.dto.response.AccountSummaryResponse;
import com.bupt.charging.dto.response.LoginResponse;
import com.bupt.charging.entity.CarAccount;
import com.bupt.charging.repository.CarAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AccountService {

    private final CarAccountRepository carAccountRepository;

    public AccountService(CarAccountRepository carAccountRepository) {
        this.carAccountRepository = carAccountRepository;
    }

    @Transactional
    public boolean createNewAccount(CreateAccountRequest request) {
        if (carAccountRepository.existsById(request.getCarId())
                || carAccountRepository.existsByUserName(request.getUserName())) {
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

    public Optional<LoginResponse> login(LoginRequest request) {
        return carAccountRepository.findByUserName(request.getUserName())
                .filter(account -> account.isRegistered())
                .filter(account -> Objects.equals(account.getPassword(), request.getPassword()))
                .map(account -> new LoginResponse(
                        account.getCarId(),
                        account.getUserName(),
                        account.getCarCapacity()));
    }

    public List<AccountSummaryResponse> listAllAccounts() {
        return carAccountRepository.findAll().stream()
                .map(AccountSummaryResponse::from)
                .toList();
    }

    public CarAccount getAccount(String carId) {
        return carAccountRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("车辆账号不存在: " + carId));
    }
}
