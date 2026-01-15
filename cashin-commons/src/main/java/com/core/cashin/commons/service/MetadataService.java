package com.core.cashin.commons.service;

import java.util.Map;

public interface MetadataService {

    Map<String, String> retrieveGatewayMetadata(String connector);

}
