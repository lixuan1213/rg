package com.bupt.charging.repository;

import com.bupt.charging.entity.BillingRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingRuleRepository extends JpaRepository<BillingRule, Long> {
}
