package com.core.cashin.commons.repository;

import com.core.cashin.commons.entity.MerchantFeeEntity;
import com.core.cashin.commons.model.MerchantFeePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MerchantFeeRepository extends JpaRepository<MerchantFeeEntity, Long> {

    @Query("""
                SELECT new com.core.cashin.commons.model.MerchantFeePayment(mf, p, py)
                FROM MerchantFeeEntity mf
                JOIN mf.paymentEntity p
                JOIN p.payerEntity py
                WHERE p.id = :paymentId
            """)
    Optional<MerchantFeePayment> findFeeByPaymentId(@Param("paymentId") Long paymentId);

}
