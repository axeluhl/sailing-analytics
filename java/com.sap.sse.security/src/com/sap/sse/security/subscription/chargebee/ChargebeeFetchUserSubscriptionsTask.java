package com.sap.sse.security.subscription.chargebee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.subscription.SubscriptionApiBaseService;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;

public class ChargebeeFetchUserSubscriptionsTask implements ChargebeeSubscriptionListRequest.OnResultListener {
    private static final Logger logger = Logger.getLogger(ChargebeeFetchUserSubscriptionsTask.class.getName());

    @FunctionalInterface
    public static interface OnResultListener {
        void onSubscriptionsResult(Iterable<Subscription> subscriptions);
    }

    private final User user;
    private final SubscriptionApiRequestProcessor requestProcessor;
    private final OnResultListener listener;

    private List<Subscription> userSubscriptions;
    private final SubscriptionApiBaseService chargebeeApiServiceParams;

    public ChargebeeFetchUserSubscriptionsTask(User user, SubscriptionApiRequestProcessor requestProcessor,
            OnResultListener listener, SubscriptionApiBaseService chargebeeApiServiceParams) {
        this.user = user;
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
            List<Subscription> subscriptionList = new ArrayList<Subscription>();
            for (ChargebeeApiSubscriptionData sub : subscriptions) {
                subscriptionList.add(sub.toSubscription());
            }
            // Sort subscription list by created date, so newest item goes first in the list
            Collections.sort(subscriptionList, (s1, s2) -> {
                return s1.getSubscriptionCreatedAt().compareTo(s2.getSubscriptionCreatedAt()) * -1;
            });
            if (userSubscriptions == null) {
                userSubscriptions = new ArrayList<Subscription>();
            }
            userSubscriptions.addAll(subscriptionList);
        }
        if (nextOffset == null || nextOffset.isEmpty()) {
            onDone();
        } else {
            fetchSubscriptionList(nextOffset);
        }
    }

    private void fetchSubscriptionList(String offset) {
        logger.info(() -> "Schedule fetch Chargebee subscriptions, user: " + user.getName() + ", offset: "
                + (offset == null ? "" : offset));
        requestProcessor.addRequest(new ChargebeeSubscriptionListRequest(user, offset, this, requestProcessor, chargebeeApiServiceParams));
    }

    private void onDone() {
        if (listener != null) {
            listener.onSubscriptionsResult(userSubscriptions);
        }
    }
}
