package com.bupt.charging.repository;

import com.bupt.charging.entity.ChargingRequest;
import com.bupt.charging.enums.CarState;
import com.bupt.charging.enums.ChargingMode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChargingRequestRepository extends JpaRepository<ChargingRequest, Long> {

    Optional<ChargingRequest> findFirstByCarIdAndActiveTrue(String carId);

    List<ChargingRequest> findByRequestModeAndCarStateAndActiveTrueOrderByRequestTimeAsc(
            ChargingMode mode, CarState carState);

    List<ChargingRequest> findByRequestModeAndCarStateAndActiveTrueOrderByPriorityDescRequestTimeAsc(
            ChargingMode mode, CarState carState);

    List<ChargingRequest> findByPileIdAndCarStateAndActiveTrue(String pileId, CarState carState);

    List<ChargingRequest> findByPileIdAndCarStateAndActiveTrueOrderByQueueNumAsc(
            String pileId, CarState carState);

    long countByPileIdAndCarStateAndActiveTrue(String pileId, CarState carState);

    long countByPileIdAndActiveTrueAndCarStateIn(String pileId, Collection<CarState> carStates);

    List<ChargingRequest> findByPileIdAndActiveTrueAndCarStateIn(
            String pileId, Collection<CarState> carStates);

    List<ChargingRequest> findByCarStateAndActiveTrue(CarState carState);

    List<ChargingRequest> findByRequestModeAndActiveTrueAndCarStateIn(
            ChargingMode mode, Collection<CarState> carStates);
}
