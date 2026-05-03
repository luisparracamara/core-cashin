package com.core.cashin.commons.utils;

import com.core.cashin.commons.exception.InternalServerException;
import com.core.cashin.commons.model.DepositRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class Utils {

    private final ObjectMapper mapper;

    public Utils() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize object to JSON {}", e.getMessage());
            throw new InternalServerException("Failed to serialize object to JSON: " + e);
        }
    }

    public String toJsonSafe(DepositRequest request) {
        try {
            ObjectNode node = mapper.valueToTree(request);
            JsonNode metadataNode = node.get("gatewayMetadata");
            if (metadataNode instanceof ObjectNode metadata) {
                List<String> keys = new ArrayList<>();
                metadata.fieldNames().forEachRemaining(keys::add);
                keys.forEach(key -> metadata.put(key, "***"));
            }
            return mapper.writeValueAsString(node);
        } catch (Exception e) {
            log.error("Failed to serialize object to JSON safely {}", e.getMessage());
            throw new InternalServerException("Failed to serialize object to JSON: " + e);
        }
    }
}
