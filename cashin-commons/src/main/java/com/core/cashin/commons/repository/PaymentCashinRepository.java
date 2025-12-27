package com.core.cashin.commons.repository;

import com.core.cashin.commons.entity.PaymentCashinEntity;
import com.core.cashin.commons.model.PaymentCashinWithGateway;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentCashinRepository extends JpaRepository<PaymentCashinEntity, Long> {

    @Query("""
                SELECT new com.core.cashin.commons.model.PaymentCashinWithGateway(
                    pc,
                    g.connectorName
                )
                FROM PaymentCashinEntity pc
                JOIN pc.paymentEntity p
                JOIN p.gateway g
                WHERE p.status = PENDING
                  AND p.updatedAt < :cutoff
                ORDER BY p.updatedAt
            """)
    List<PaymentCashinWithGateway> findPendingPaymentCashinForReconciliation(
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );

}
