package com.core.cashin.payment.methods.methods.stripe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StripeTokenResponse {

    @JsonProperty("access_token")           private String accessToken;
    @JsonProperty("refresh_token")          private String refreshToken;
    @JsonProperty("token_type")             private String tokenType;
    @JsonProperty("stripe_user_id")         private String stripeUserId;
    @JsonProperty("stripe_publishable_key") private String stripePublishableKey;
    @JsonProperty("scope")                  private String scope;
    @JsonProperty("livemode")               private Boolean liveMode;

}
