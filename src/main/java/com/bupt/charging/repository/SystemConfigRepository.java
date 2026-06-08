package com.bupt.charging.repository;

import com.bupt.charging.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
}
