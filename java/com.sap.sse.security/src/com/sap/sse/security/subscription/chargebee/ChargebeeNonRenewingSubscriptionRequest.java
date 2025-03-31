package com.sap.sse.security.subscription.chargebee;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.chargebee.Result;
import com.chargebee.models.Subscription;
import com.chargebee.models.Subscription.CancelForItemsRequest;
import com.sap.sse.security.subscription.SubscriptionApiBaseService;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;

/**
 * Non Renewing Chargebee subscription request.
 * Sets the subscription to non renewing.
 */
public class ChargebeeNonRenewingSubscriptionRequest extends ChargebeeApiRequest {
    private static final Logger logger = Logger.getLogger(ChargebeeNonRenewingSubscriptionRequest.class.getName());

    public static interface OnResultListener {
        void onSubscriptionNonRenewingResult(Subscription subscription);
    }

    private final String subscriptionId;
    private final OnResultListener listener;

    public ChargebeeNonRenewingSubscriptionRequest(String subscriptionId, OnResultListener listener,
            SubscriptionApiRequestProcessor requestProcessor, SubscriptionApiBaseService chargebeeApiServiceParams) {
        super(requestProcessor, chargebeeApiServiceParams);
        this.subscriptionId = subscriptionId;
        this.listener = listener;
    }

    @Override
    protected ChargebeeInternalApiRequestWrapper createRequest() {
        logger.info(() -> "Set Chargebee subscription to non renewing, subscription id: " + subscriptionId);
        CancelForItemsRequest request = Subscription.cancelForItems(subscriptionId).endOfTerm(true);
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
        logger.log(Level.SEVERE, "Setting Chargebee subscription to non renewing failed, subscription id: " + subscriptionId, e);
        onDone(null);
    }

    private void onDone(Subscription subscription) {
        if (listener != null) {
            listener.onSubscriptionNonRenewingResult(subscription);
        }
    }
}
