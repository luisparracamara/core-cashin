package com.core.cashin.commons.repository;

import com.core.cashin.commons.entity.GatewayMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.core.cashin.commons.entity.Gateway;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MetadataRepository extends JpaRepository<GatewayMetadataEntity, Long> {

    @Query("SELECT gme FROM GatewayMetadataEntity gme " +
            "JOIN gme.gateway g " +
            "WHERE g.connectorName = :connectorName " +
            "AND g.merchantId = :merchantId")
    List<GatewayMetadataEntity> findByGatewayConnectorName(@Param("connectorName") String connectorName,
                                                           @Param("merchantId") Long merchantId);

    @Query("SELECT gme FROM GatewayMetadataEntity gme " +
            "JOIN gme.gateway g " +
            "WHERE g.connectorName = :connectorName AND g.merchantId = :merchantId AND gme.metaKey = :metaKey")
    Optional<GatewayMetadataEntity> findByGatewayConnectorNameAndMetaKey(
            @Param("connectorName") String connectorName, @Param("merchantId") Long merchantId,
            @Param("metaKey") String metaKey);

    @Query("SELECT g FROM Gateway g WHERE g.connectorName = :connectorName AND g.merchantId = :merchantId")
    Optional<Gateway> findGatewayByConnectorName(@Param("connectorName") String connectorName,
                                                 @Param("merchantId") Long merchantId);

    @Query("SELECT g FROM Gateway g WHERE g.connectorName IN :connectorNames")
    List<Gateway> findOAuthCapableGateways(@Param("connectorNames") Set<String> connectorNames);

}
