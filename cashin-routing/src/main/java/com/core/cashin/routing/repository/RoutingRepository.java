package com.core.cashin.routing.repository;

import com.core.cashin.commons.model.RoutingResultProjection;
import com.core.cashin.routing.entity.RoutingGateway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutingRepository extends JpaRepository<RoutingGateway, Long> {

    @Query("""
    SELECT
      pm.id AS paymentMethodId,
      pm.code AS paymentCode,
      m.id AS merchantId,
      m.name AS merchantName,
      g.id AS gatewayId,
      g.connectorName AS connectorName,
      gpm.currency AS currency,
      gm.metaKey AS metadataKey,
      gm.metaValue AS metadataValue
    FROM Merchant m
    JOIN RoutingGateway rg
        ON rg.merchant.id = m.id AND rg.status = 'ENABLED'
    JOIN RoutingRule rr
        ON rr.id = rg.routingRule.id AND rr.country = :country
    JOIN PaymentMethod pm
        ON pm.id = rr.paymentMethod.id
       AND pm.code = :code
       AND pm.status = 'ENABLED'
    JOIN Gateway g
        ON g.id = rg.gateway.id
    JOIN GatewayPaymentMethod gpm
       ON gpm.gateway.id = g.id AND gpm.paymentMethod.id = pm.id AND gpm.currency = :currency
    LEFT JOIN g.metadata gm
    WHERE m.loginId = :loginId
      AND m.secretKey = :secretKey
      AND m.status = 'ENABLED'
""")
    List<RoutingResultProjection> resolveRouting(
            String country,
            String code,
            String loginId,
            String secretKey,
            String currency
    );


}
