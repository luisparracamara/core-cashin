package com.core.cashin.payment.methods.model;

import lombok.Builder;

@Builder
public record MercadoPagoCheckoutProPreferenceResponse (
        String additionalInfo,
        String clientId,
        Long checkoutCollectorId,
        String dateCreated,
        String externalReference,
        String id,
        String redirectUrl,
        String notificationUrl,
        String operationType,
        String sandboxInitPoint
) {

}
