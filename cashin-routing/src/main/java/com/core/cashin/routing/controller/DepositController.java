package com.core.cashin.routing.controller;

import com.core.cashin.commons.model.DepositRequest;
import com.core.cashin.commons.model.DepositResponse;
import com.core.cashin.routing.service.RoutingService;
import com.core.cashin.commons.utils.Utils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class DepositController {

    private final RoutingService routingService;
    private final Utils utils;

    public DepositController(RoutingService routingService, Utils utils) {
        this.routingService = routingService;
        this.utils = utils;
    }

    @PostMapping("/v1/deposit")
    public ResponseEntity<DepositResponse> createDeposit(@RequestBody @Valid DepositRequest depositRequest,
                                                         @RequestHeader Map<String, String> headers) {
        log.debug("Request CONTROLLER {}", utils.toJson(depositRequest));
        DepositResponse depositResponse = routingService.createDeposit(depositRequest, headers);
        log.debug("Response CONTROLLER {}", utils.toJson(depositResponse));
        return ResponseEntity.ok(depositResponse);
    }
}
