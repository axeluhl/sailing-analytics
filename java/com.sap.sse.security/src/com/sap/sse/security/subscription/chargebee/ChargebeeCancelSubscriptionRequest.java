package com.sap.sse.security.subscription.chargebee;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.chargebee.Result;
import com.chargebee.models.Subscription;
import com.chargebee.models.Subscription.CancelForItemsRequest;
import com.chargebee.models.enums.CreditOptionForCurrentTermCharges;
import com.chargebee.models.enums.RefundableCreditsHandling;
import com.sap.sse.security.subscription.SubscriptionApiBaseService;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;

/**
 * Cancel Chargebee subscription request
 */
public class ChargebeeCancelSubscriptionRequest extends ChargebeeApiRequest {
    private static final Logger logger = Logger.getLogger(ChargebeeCancelSubscriptionRequest.class.getName());

    public static interface OnResultListener {
        void onSubscriptionCancelResult(Subscription subscription);
    }

    private final String subscriptionId;
    private final OnResultListener listener;

    public ChargebeeCancelSubscriptionRequest(String subscriptionId, OnResultListener listener,
            SubscriptionApiRequestProcessor requestProcessor, SubscriptionApiBaseService chargebeeApiServiceParams) {
        super(requestProcessor, chargebeeApiServiceParams);
        this.subscriptionId = subscriptionId;
        this.listener = listener;
    }

    @Override
    protected ChargebeeInternalApiRequestWrapper createRequest() {
        logger.info(() -> "Cancel Chargebee subscription, subscription id: " + subscriptionId);
        CancelForItemsRequest request = Subscription.cancelForItems(subscriptionId)
                .creditOptionForCurrentTermCharges(CreditOptionForCurrentTermCharges.PRORATE)
                .refundableCreditsHandling(RefundableCreditsHandling.SCHEDULE_REFUND)
                .endOfTerm(false);
        return new ChargebeeInternalApiRequestWrapper(request);
    }

    @Override
    protected void processResult(ChargebeeInternalApiRequestWrapper request) {
        Result result = request.getResult();
        if (result != null) {
            onDone(result.subscription());
        } else {
            onDone(null);
        }
    }

    @Override
    protected void handleError(Exception e) {
        logger.log(Level.SEVERE, "Cancel Chargebee subscription failed, subscription id: " + subscriptionId, e);
        onDone(null);
    }

    private void onDone(Subscription subscription) {
        if (listener != null) {
            listener.onSubscriptionCancelResult(subscription);
        }
    }
}
