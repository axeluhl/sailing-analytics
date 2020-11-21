package com.sap.sse.security.shared.subscription.chargebee;

import com.sap.sse.security.shared.subscription.AbstractSubscriptionDataHandler;
import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.SubscriptionData;

public class ChargebeeSubscriptionDataHandler extends AbstractSubscriptionDataHandler {

    @Override
    public Subscription toSubscription(SubscriptionData data) {
        return new ChargebeeSubscription(data.getSubscriptionId(), data.getPlanId(), data.getCustomerId(),
                data.getTrialStart(), data.getTrialEnd(), data.getSubscriptionStatus(), data.getPaymentStatus(),
                data.getTransactionType(), data.getTransactionStatus(), data.getInvoiceId(), data.getInvoiceStatus(),
                data.getSubscriptionCreatedAt(), data.getSubscriptionUpdatedAt(), data.getLatestEventTime(),
                data.getManualUpdatedAt());
    }
}
