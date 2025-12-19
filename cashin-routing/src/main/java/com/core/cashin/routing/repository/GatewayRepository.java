package com.core.cashin.routing.repository;

import com.core.cashin.routing.entity.Gateway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GatewayRepository extends JpaRepository<Gateway, Long> {

    @Query("""
        SELECT g
        FROM PaymentMethod pm
        JOIN GatewayPaymentMethod gpm
            ON gpm.paymentMethod.id = pm.id
        JOIN Gateway g
            ON g.id = gpm.gateway.id
        LEFT JOIN FETCH g.metadata
        WHERE pm.code = :code
          AND pm.country = :country
          AND pm.status = 'ENABLED'
    """)
    Optional<Gateway> findGatewayWithMetadata(
            @Param("code") String code,
            @Param("country") String country
    );

}
