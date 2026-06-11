package com.bupt.charging.repository;

import com.bupt.charging.entity.CarAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarAccountRepository extends JpaRepository<CarAccount, String> {

    Optional<CarAccount> findByUserName(String userName);

    boolean existsByUserName(String userName);
}
