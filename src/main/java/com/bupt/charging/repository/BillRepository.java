package com.bupt.charging.repository;

import com.bupt.charging.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByCarIdAndDate(String carId, LocalDate date);
}
