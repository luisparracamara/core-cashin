package com.core.cashin.routing.controller;

import com.core.cashin.commons.model.OAuthTokenResponse;
import com.core.cashin.routing.service.OAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/oauth")
@Slf4j
public class OAuthController {

    private final OAuthService oAuthService;

    public OAuthController(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @GetMapping("/auth-url")
    public ResponseEntity<Map<String, String>> getAuthUrl(
            @RequestHeader("X-Merchant-Id") Long merchantId,
            @RequestParam String connector,
            @RequestParam String state) {
        log.debug("[OAuthController] getAuthUrl connector={} merchantId={} state={}", connector, merchantId, state);
        String authUrl = oAuthService.getAuthUrl(connector, merchantId, state);
        return ResponseEntity.ok(Map.of("authUrl", authUrl));
    }

    @GetMapping("/callback")
    public ResponseEntity<OAuthTokenResponse> callback(
            @RequestParam String code,
            @RequestParam String state) {
        log.debug("[OAuthController] callback state={}", state);
        OAuthTokenResponse response = oAuthService.handleCallback(code, state);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{connector}/status")
    public ResponseEntity<Map<String, Object>> status(
            @PathVariable String connector,
            @RequestHeader("X-Merchant-Id") Long merchantId) {
        log.debug("[OAuthController] status connector={} merchantId={}", connector, merchantId);
        boolean connected = oAuthService.isConnected(connector, merchantId);
        return ResponseEntity.ok(Map.of("connected", connected, "connector", connector));
    }

    @DeleteMapping("/{connector}/disconnect")
    public ResponseEntity<Map<String, Object>> disconnect(
            @PathVariable String connector,
            @RequestHeader("X-Merchant-Id") Long merchantId) {
        log.debug("[OAuthController] disconnect connector={} merchantId={}", connector, merchantId);
        oAuthService.disconnect(connector, merchantId);
        return ResponseEntity.ok(Map.of("disconnected", true, "connector", connector));
    }

    @PostMapping("/{connector}/refresh")
    public ResponseEntity<OAuthTokenResponse> refresh(
            @PathVariable String connector,
            @RequestHeader("X-Merchant-Id") Long merchantId) {
        log.debug("[OAuthController] refresh connector={} merchantId={}", connector, merchantId);
        OAuthTokenResponse response = oAuthService.refresh(connector, merchantId);
        return ResponseEntity.ok(response);
    }

}
