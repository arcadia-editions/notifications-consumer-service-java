package com.arcadiaeditions.notifications.consumer.adapters.events;

import com.arcadiaeditions.notifications.consumer.asyncapi.client.IPaymentCapturedEventV1ConsumerService;
import com.arcadiaeditions.notifications.consumer.core.inbound.NotificationsConsumerService;
import com.arcadiaeditions.payments.processing.events.avro.PaymentCaptured;
import org.springframework.stereotype.Component;

/**
 * Inbound adapter: payment-captured-event-v1 -> NotificationsConsumerService.sendOrderConfirmation.
 * Stub so the Spring context boots; the generator does not emit adapters for AsyncAPIGenerator interfaces.
 */
@Component
public class PaymentCapturedConsumerService implements IPaymentCapturedEventV1ConsumerService {

    private final NotificationsConsumerService notificationsConsumerService;

    public PaymentCapturedConsumerService(NotificationsConsumerService notificationsConsumerService) {
        this.notificationsConsumerService = notificationsConsumerService;
    }

    @Override
    public void onPaymentCapturedEventV1(PaymentCaptured payload, PaymentCapturedHeaders headers) {
        // TODO CUSTOM_REQUIRED: map payload to SendOrderConfirmationInput and call
        // notificationsConsumerService.sendOrderConfirmation
        throw new UnsupportedOperationException("Not implemented");
    }
}
