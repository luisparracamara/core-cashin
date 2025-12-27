package com.core.cashin.commons.repository;

import com.core.cashin.commons.entity.GatewayMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MetadataRepository extends JpaRepository<GatewayMetadataEntity, Long> {

    @Query("SELECT gme FROM GatewayMetadataEntity gme " +
            "JOIN gme.gateway g " +
            "WHERE g.connectorName = :connectorName")
    List<GatewayMetadataEntity> findByGatewayConnectorName(@Param("connectorName") String connectorName);

}
