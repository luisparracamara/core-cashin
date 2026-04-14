package com.core.cashin.payment.methods.methods.mercadopagocheckoutpro;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MercadoPagoTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private Long expiresIn;

    private String scope;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("public_key")
    private String publicKey;

    @JsonProperty("live_mode")
    private Boolean liveMode;

}
