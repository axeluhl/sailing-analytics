package com.sap.sse.security.subscription.chargebee;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.chargebee.Result;
import com.chargebee.models.PortalSession;
import com.chargebee.models.PortalSession.CreateRequest;
import com.sap.sse.security.subscription.SubscriptionApiBaseService;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;

/**
 * Request to fetch Chargebee subscription by subscription id
 */
public class ChargebeeSelfServicePortalSessionRequest extends ChargebeeApiRequest {
    private static final Logger logger = Logger.getLogger(ChargebeeSelfServicePortalSessionRequest.class.getName());

    public static interface OnResultListener {
        void onSessionResult(PortalSession session);
    }

    private final String userId;
    private final OnResultListener listener;

    public ChargebeeSelfServicePortalSessionRequest(String userId, OnResultListener listener,
            SubscriptionApiRequestProcessor requestProcessor, SubscriptionApiBaseService chargebeeApiServiceParams) {
        super(requestProcessor, chargebeeApiServiceParams);
        this.userId = userId;
        this.listener = listener;
    }

    @Override
    protected ChargebeeInternalApiRequestWrapper createRequest() {
        logger.info(() -> "Fetch Chargebee Self-Service Portal session, user id: " + userId);
        CreateRequest request = PortalSession.create()
                .customerId(userId);
        return new ChargebeeInternalApiRequestWrapper(request);
    }

    @Override
    protected void processResult(ChargebeeInternalApiRequestWrapper request) {
        Result result = request.getResult();
        final PortalSession session;
        if (result != null) {
            session = result.portalSession();
        } else {
            session = null;
        }
        onDone(session);
    }

    @Override
    protected void handleError(Exception e) {
        logger.log(Level.SEVERE, "Fetch Chargebee Self-Service Portal session failed, user id: " + userId, e);
        onDone(null);
    }

    private void onDone(PortalSession session) {
        if (listener != null) {
            listener.onSessionResult(session);
        }
    }
}
