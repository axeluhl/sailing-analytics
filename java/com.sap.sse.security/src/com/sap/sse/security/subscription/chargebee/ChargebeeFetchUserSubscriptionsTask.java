package com.sap.sse.security.subscription.chargebee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscription;
import com.sap.sse.security.subscription.SubscriptionApiBaseService;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;

public class ChargebeeFetchUserSubscriptionsTask implements ChargebeeSubscriptionListRequest.OnResultListener {
    private static final Logger logger = Logger.getLogger(ChargebeeFetchUserSubscriptionsTask.class.getName());

    @FunctionalInterface
    public static interface OnResultListener {
        void onSubscriptionsResult(Map<String, List<Subscription>> userSubscriptions);
    }

    private final SubscriptionApiRequestProcessor requestProcessor;
    private final OnResultListener listener;

    private Map<String, List<Subscription>> userSubscriptions;
    private final SubscriptionApiBaseService chargebeeApiServiceParams;

    public ChargebeeFetchUserSubscriptionsTask(SubscriptionApiRequestProcessor requestProcessor,
            OnResultListener listener, SubscriptionApiBaseService chargebeeApiServiceParams) {
        this.requestProcessor = requestProcessor;
        this.listener = listener;
        this.chargebeeApiServiceParams = chargebeeApiServiceParams;
    }

    public void run() {
        fetchSubscriptionList(null);
    }

    @Override
    public void onSubscriptionListResult(Iterable<ChargebeeApiSubscriptionData> subscriptions, String nextOffset) {
        if (subscriptions != null) {
            if (userSubscriptions == null) {
                userSubscriptions = new HashMap<>();
            }
            for (ChargebeeApiSubscriptionData sub : subscriptions) {
                final ChargebeeSubscription subscription = sub.toSubscription(chargebeeApiServiceParams.getSubscriptionPlanProvider());
                final String customerId = subscription.getCustomerId();
                final List<Subscription> list;
                if (userSubscriptions.containsKey(customerId)) {
                    list = userSubscriptions.get(customerId);
                } else {
                    list = new ArrayList<>();
                    userSubscriptions.put(customerId, list);
                }
                list.add(subscription);
            }
        } else { 
            // An error might have occurred, nullifying result;
            userSubscriptions = null;
            onDone();
        }
        if (nextOffset == null || nextOffset.isEmpty()) {
            onDone();
        } else {
            fetchSubscriptionList(nextOffset);
        }
    }

    private void fetchSubscriptionList(String offset) {
        logger.info(() -> "Schedule fetch Chargebee subscriptions, offset: "
                + (offset == null ? "" : offset));
        requestProcessor.addRequest(new ChargebeeSubscriptionListRequest(offset, this, requestProcessor, chargebeeApiServiceParams));
    }

    private void onDone() {
        if (listener != null) {
            listener.onSubscriptionsResult(userSubscriptions);
        }
    }
}
