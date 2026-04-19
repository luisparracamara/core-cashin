package com.core.cashin.commons.service;

import java.util.Map;

public interface MetadataService {

    Map<String, String> retrieveGatewayMetadata(String connector, Long merchantId);

    void saveOrUpdateMetadata(String connector, Long merchantId, String key, String value);

    void deleteMetadata(String connector, Long merchantId, String key);

}
