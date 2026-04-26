package com.core.cashin.commons.service.impl;

import com.core.cashin.commons.entity.Gateway;
import com.core.cashin.commons.entity.GatewayMetadataEntity;
import com.core.cashin.commons.exception.NotFoundException;
import com.core.cashin.commons.repository.MetadataRepository;
import com.core.cashin.commons.service.MetadataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MetadataServiceImpl implements MetadataService {

    private final MetadataRepository metadataRepository;

    public MetadataServiceImpl(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    @Override
    public Map<String, String> retrieveGatewayMetadata(String connector, Long merchantId) {
        return metadataRepository.findByGatewayConnectorName(connector, merchantId)
                .stream()
                .collect(Collectors.toMap(
                        GatewayMetadataEntity::getMetaKey,
                        GatewayMetadataEntity::getMetaValue
                ));
    }

    @Override
    @Transactional
    public void saveOrUpdateMetadata(String connector, Long merchantId, String key, String value) {
        List<GatewayMetadataEntity> existing = metadataRepository
                .findByGatewayConnectorNameAndMetaKey(connector, merchantId, key);

        if (!existing.isEmpty()) {
            metadataRepository.deleteAll(existing);
            metadataRepository.flush();
        }

        Gateway gateway = metadataRepository.findGatewayByConnectorName(connector, merchantId)
                .orElseThrow(() -> new NotFoundException("Gateway not found for connector: " + connector + " merchantId: " + merchantId));

        metadataRepository.save(GatewayMetadataEntity.builder()
                .gateway(gateway)
                .metaKey(key)
                .metaValue(value)
                .build());
    }

    @Override
    @Transactional
    public void deleteMetadata(String connector, Long merchantId, String key) {
        List<GatewayMetadataEntity> existing = metadataRepository
                .findByGatewayConnectorNameAndMetaKey(connector, merchantId, key);
        if (!existing.isEmpty()) {
            metadataRepository.deleteAll(existing);
        }
    }

}
