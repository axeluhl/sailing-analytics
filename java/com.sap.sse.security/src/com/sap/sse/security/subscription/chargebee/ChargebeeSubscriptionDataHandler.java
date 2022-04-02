package com.sap.sse.security.subscription.chargebee;

import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscription;
import com.sap.sse.security.subscription.AbstractSubscriptionDataHandler;
import com.sap.sse.security.subscription.SubscriptionData;

public class ChargebeeSubscriptionDataHandler extends AbstractSubscriptionDataHandler {
    @Override
    public Subscription toSubscription(SubscriptionData data) {
        return new ChargebeeSubscription(data.getSubscriptionId(), data.getPlanId(), data.getCustomerId(),
                data.getTrialStart(), data.getTrialEnd(), data.getSubscriptionStatus(), data.getPaymentStatus(),
                data.getTransactionType(), data.getTransactionStatus(), data.getInvoiceId(), data.getInvoiceStatus(),
                data.getReocurringPaymentValue(), data.getCurrencyCode(), data.getSubscriptionCreatedAt(),
                data.getSubscriptionUpdatedAt(), data.getSubscriptionActivatedAt(), data.getNextBillingAt(),
                data.getCurrentTermEnd(), data.getCancelledAt(), data.getLatestEventTime(), data.getManualUpdatedAt());
    }
}
