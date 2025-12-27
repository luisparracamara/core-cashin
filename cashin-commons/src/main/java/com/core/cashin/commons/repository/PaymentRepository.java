package com.core.cashin.commons.repository;

import com.core.cashin.commons.constants.PaymentStatusEnum;
import com.core.cashin.commons.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    @Modifying
    @Query("""
            UPDATE PaymentEntity
              SET status = :nextStatus,
              updatedAt = NOW()
             WHERE status = :originalStatus
             AND updatedAt < :cutoff
            """)
    int updatePaymentStatus(@Param("nextStatus") PaymentStatusEnum nextStatus,
                            @Param("cutoff") LocalDateTime cutoff,
                            @Param("originalStatus") PaymentStatusEnum originalStatus);

    @Modifying
    @Query("""
            UPDATE PaymentEntity p
               SET p.status = :status,
                   p.updatedAt = NOW()
             WHERE p.id IN :ids
            """)
    int cancelPaymentStatus(@Param("status") PaymentStatusEnum status,
                       @Param("ids") List<Long> ids);

}
