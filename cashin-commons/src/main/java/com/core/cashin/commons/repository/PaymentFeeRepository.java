package com.core.cashin.commons.repository;

import com.core.cashin.commons.entity.PaymentFeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentFeeRepository extends JpaRepository<PaymentFeeEntity, Long> {
}
