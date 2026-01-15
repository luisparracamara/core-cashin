package com.core.cashin.commons.service.impl;

import com.core.cashin.commons.entity.GatewayMetadataEntity;
import com.core.cashin.commons.repository.MetadataRepository;
import com.core.cashin.commons.service.MetadataService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MetadataServiceImpl implements MetadataService {

    private final MetadataRepository metadataRepository;

    public MetadataServiceImpl(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    @Override
    public Map<String, String> retrieveGatewayMetadata(String connector) {
        return metadataRepository.findByGatewayConnectorName(connector)
                .stream()
                .collect(Collectors.toMap(
                        GatewayMetadataEntity::getMetaKey,
                        GatewayMetadataEntity::getMetaValue
                ));
    }

}
