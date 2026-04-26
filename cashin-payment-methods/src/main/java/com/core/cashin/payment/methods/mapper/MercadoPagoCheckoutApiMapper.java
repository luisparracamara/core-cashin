package com.core.cashin.payment.methods.mapper;

import com.core.cashin.commons.entity.PaymentCashinEntity;
import com.core.cashin.commons.entity.PaymentEntity;
import com.core.cashin.commons.model.DepositMetadataResponse;
import com.core.cashin.commons.model.DepositRequest;
import com.core.cashin.commons.model.DepositResponse;
import com.core.cashin.commons.model.PaymentInfoResponse;
import com.core.cashin.payment.methods.methods.mercadopagocheckoutapi.MercadoPagoCheckoutApiConstants;
import com.core.cashin.payment.methods.methods.mercadopagocheckoutapi.MercadoPagoOrderRequest;
import com.core.cashin.payment.methods.methods.mercadopagocheckoutapi.MercadoPagoOrderResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class MercadoPagoCheckoutApiMapper {

    public MercadoPagoOrderRequest buildOrderRequest(DepositRequest request, String paymentId) {
        Map<String, String> paymentData = request.getPaymentData();
        String cardType = paymentData.get(MercadoPagoCheckoutApiConstants.CARD_TYPE);
        String cardToken = paymentData.get(MercadoPagoCheckoutApiConstants.CARD_TOKEN);
        String cardBrand = paymentData.get(MercadoPagoCheckoutApiConstants.CARD_BRAND);

        MercadoPagoOrderRequest.OrderPaymentMethod paymentMethod = buildPaymentMethod(
                cardBrand, cardType, cardToken);

        MercadoPagoOrderRequest.OrderPayment payment = new MercadoPagoOrderRequest.OrderPayment(
                request.getAmount().toPlainString(),
                paymentMethod
        );

        MercadoPagoOrderRequest.OrderPayer payer = new MercadoPagoOrderRequest.OrderPayer(
                request.getPayer().getEmail()
        );

        return new MercadoPagoOrderRequest(
                MercadoPagoCheckoutApiConstants.ORDER_TYPE,
                MercadoPagoCheckoutApiConstants.PROCESSING_MODE,
                paymentId,
                request.getAmount().toPlainString(),
                "Pago " + request.getMerchant().getMerchantName(),
                payer,
                new MercadoPagoOrderRequest.OrderTransactions(List.of(payment))
        );
    }

    private MercadoPagoOrderRequest.OrderPaymentMethod buildPaymentMethod(String paymentMethodId,
                                                                           String cardType, String cardToken) {
        if (MercadoPagoCheckoutApiConstants.PAYMENT_TYPE_ACCOUNT_MONEY.equals(cardType)) {
            return new MercadoPagoOrderRequest.OrderPaymentMethod(
                    MercadoPagoCheckoutApiConstants.PAYMENT_TYPE_ACCOUNT_MONEY,
                    MercadoPagoCheckoutApiConstants.PAYMENT_TYPE_ACCOUNT_MONEY,
                    null,
                    null
            );
        }
        return new MercadoPagoOrderRequest.OrderPaymentMethod(
                paymentMethodId,
                cardType,
                cardToken,
                MercadoPagoCheckoutApiConstants.DEFAULT_INSTALLMENTS
        );
    }

    public DepositResponse buildDepositResponse(DepositRequest request, MercadoPagoOrderResponse orderResponse,
                                                 Long paymentId, String cashInMethod) {
        DepositMetadataResponse metadata = DepositMetadataResponse.builder()
                .payerName(request.getPayer().getFirstName() + " " + request.getPayer().getLastName())
                .beneficiaryName(request.getMerchant().getMerchantName())
                .build();

        PaymentInfoResponse paymentInfo = PaymentInfoResponse.builder()
                .type(cashInMethod)
                .paymentMethod(request.getPaymentMethod())
                .paymentMethodName(request.getPaymentMethodName())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .createdAt(orderResponse.createdDate())
                .metadata(metadata)
                .build();

        return DepositResponse.builder()
                .depositId(paymentId)
                .checkoutType(MercadoPagoCheckoutApiConstants.CHECKOUT_TYPE)
                .paymentInfo(paymentInfo)
                .build();
    }

    public PaymentCashinEntity buildPaymentCashinEntity(MercadoPagoOrderResponse orderResponse,
                                                         PaymentEntity paymentEntity,
                                                         LocalDateTime now, String json) {
        return PaymentCashinEntity.builder()
                .data(json)
                .externalReference(orderResponse.id())
                .createdAt(now)
                .updatedAt(now)
                .paymentEntity(paymentEntity)
                .build();
    }

}
