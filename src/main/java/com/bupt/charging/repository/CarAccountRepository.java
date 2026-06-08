package com.bupt.charging.repository;

import com.bupt.charging.entity.CarAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarAccountRepository extends JpaRepository<CarAccount, String> {
}
