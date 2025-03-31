package com.sap.sse.security.subscription.chargebee;

import java.util.logging.Logger;

import com.chargebee.models.PortalSession;
import com.sap.sse.security.subscription.SubscriptionApiBaseService;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;

/**
 * Task to perform canceling a subscription by subscription id
 */
public class ChargeBeeGetSelfServicePortalSessionTask implements ChargebeeSelfServicePortalSessionRequest.OnResultListener {
    private static final Logger logger = Logger.getLogger(ChargeBeeGetSelfServicePortalSessionTask.class.getName());

    /**
     * Cancel result listener
     */
    @FunctionalInterface
    public static interface OnResultListener {
        void onSessionResult(PortalSession result);
    }

    private final String userId;
    private final SubscriptionApiRequestProcessor requestProcessor;
    private final OnResultListener listener;
    private final SubscriptionApiBaseService chargebeeApiServiceParams;

    public ChargeBeeGetSelfServicePortalSessionTask(String userId, SubscriptionApiRequestProcessor requestProcessor,
            OnResultListener listener, SubscriptionApiBaseService chargebeeApiServiceParams) {
        this.userId = userId;
        this.requestProcessor = requestProcessor;
        this.listener = listener;
        this.chargebeeApiServiceParams = chargebeeApiServiceParams;
    }

    public void run() {
        logger.info(() -> "Schedule fetch of self service portal session, user id: " + userId);
        requestProcessor.addRequest(new ChargebeeSelfServicePortalSessionRequest(userId, this, requestProcessor, chargebeeApiServiceParams));
    }
    
    @Override
    public void onSessionResult(PortalSession session) {
        listener.onSessionResult(session);
    }
}
