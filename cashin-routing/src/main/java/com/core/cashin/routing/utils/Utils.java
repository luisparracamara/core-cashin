package com.core.cashin.routing.utils;

import com.core.cashin.commons.exception.InternalServerException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Utils {

    private final ObjectMapper mapper;

    public Utils() {
        this.mapper = new ObjectMapper();
    }

    public String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize object to JSON {}", e.getMessage());
            throw new InternalServerException("Failed to serialize object to JSON: "+ e);
        }
    }
}
