package com.bupt.charging.repository;

import com.bupt.charging.entity.ChargingPile;
import com.bupt.charging.enums.ChargingMode;
import com.bupt.charging.enums.PileWorkingState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChargingPileRepository extends JpaRepository<ChargingPile, String> {

    List<ChargingPile> findByMode(ChargingMode mode);

    List<ChargingPile> findByModeAndWorkingState(ChargingMode mode, PileWorkingState workingState);
}
