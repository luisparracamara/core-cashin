package com.core.cashin.routing.controller;

import com.core.cashin.commons.model.CheckStatusResponse;
import com.core.cashin.commons.model.DepositRequest;
import com.core.cashin.commons.model.DepositResponse;
import com.core.cashin.routing.service.RoutingService;
import com.core.cashin.routing.validation.ValidHeaders;
import com.core.cashin.commons.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Validated
@Slf4j
public class DepositController {

    private final RoutingService routingService;
    private final Utils utils;

    public DepositController(RoutingService routingService, Utils utils) {
        this.routingService = routingService;
        this.utils = utils;
    }

    @PostMapping(value = "/v1/deposit", produces = "application/json")
    public ResponseEntity<DepositResponse> createDeposit(@RequestBody @Valid DepositRequest depositRequest,
                                                         HttpServletRequest httpServletRequest,
                                                         @ValidHeaders @RequestHeader Map<String, String> headers) {
        log.debug("Request CONTROLLER {}", utils.toJson(depositRequest));
        DepositResponse depositResponse = routingService.createDeposit(depositRequest, httpServletRequest,  headers);
        log.debug("Response CONTROLLER {}", utils.toJson(depositResponse));
        return ResponseEntity.ok(depositResponse);
    }

    @GetMapping(value = "/v1/deposit/{depositId}", produces = "application/json")
    public ResponseEntity<CheckStatusResponse> getDeposit(@PathVariable long depositId,
                                                           @ValidHeaders @RequestHeader Map<String, String> headers) {
        log.debug("Request CONTROLLER {}", depositId);
        CheckStatusResponse checkStatusDeposit = routingService.checkStatusDeposit(depositId);
        log.debug("Response CONTROLLER {}", utils.toJson(checkStatusDeposit));
        return ResponseEntity.ok(checkStatusDeposit);
    }
    
}
