package com.core.cashin.commons.service.impl;

import com.core.cashin.commons.entity.Gateway;
import com.core.cashin.commons.entity.GatewayMetadataEntity;
import com.core.cashin.commons.exception.NotFoundException;
import com.core.cashin.commons.repository.MetadataRepository;
import com.core.cashin.commons.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MetadataServiceImpl implements MetadataService {

    private final MetadataRepository metadataRepository;

    public MetadataServiceImpl(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    @Override
    public Map<String, String> retrieveGatewayMetadata(String connector, Long merchantId) {
        log.debug("[Metadata] retrieving metadata connector={} merchantId={}", connector, merchantId);
        Map<String, String> metadata = metadataRepository.findByGatewayConnectorName(connector, merchantId)
                .stream()
                .collect(Collectors.toMap(
                        GatewayMetadataEntity::getMetaKey,
                        GatewayMetadataEntity::getMetaValue
                ));
        log.debug("[Metadata] metadata retrieved connector={} merchantId={} keys={}", connector, merchantId, metadata.keySet());
        return metadata;
    }

    @Override
    @Transactional
    public void saveOrUpdateMetadata(String connector, Long merchantId, String key, String value) {
        log.debug("[Metadata] saveOrUpdate connector={} merchantId={} key={}", connector, merchantId, key);
        List<GatewayMetadataEntity> existing = metadataRepository
                .findByGatewayConnectorNameAndMetaKey(connector, merchantId, key);

        if (!existing.isEmpty()) {
            log.debug("[Metadata] deleting existing entries connector={} merchantId={} key={} count={}",
                    connector, merchantId, key, existing.size());
            metadataRepository.deleteAll(existing);
            metadataRepository.flush();
        }

        Gateway gateway = metadataRepository.findGatewayByConnectorName(connector, merchantId)
                .orElseThrow(() -> {
                    log.error("[Metadata] gateway not found connector={} merchantId={}", connector, merchantId);
                    return new NotFoundException("Gateway not found for connector: " + connector + " merchantId: " + merchantId);
                });

        metadataRepository.save(GatewayMetadataEntity.builder()
                .gateway(gateway)
                .metaKey(key)
                .metaValue(value)
                .build());
        log.debug("[Metadata] metadata saved connector={} merchantId={} key={}", connector, merchantId, key);
    }

    @Override
    @Transactional
    public void deleteMetadata(String connector, Long merchantId, String key) {
        log.debug("[Metadata] delete connector={} merchantId={} key={}", connector, merchantId, key);
        List<GatewayMetadataEntity> existing = metadataRepository
                .findByGatewayConnectorNameAndMetaKey(connector, merchantId, key);
        if (!existing.isEmpty()) {
            metadataRepository.deleteAll(existing);
            log.debug("[Metadata] deleted connector={} merchantId={} key={} count={}", connector, merchantId, key, existing.size());
        } else {
            log.debug("[Metadata] nothing to delete connector={} merchantId={} key={}", connector, merchantId, key);
        }
    }

}
