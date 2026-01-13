package com.core.cashin.commons.repository;

import com.core.cashin.commons.entity.MerchantCostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantCostRepository extends JpaRepository<MerchantCostEntity, Long> {

    Optional<MerchantCostEntity> findByMerchantId(Long id);
}
