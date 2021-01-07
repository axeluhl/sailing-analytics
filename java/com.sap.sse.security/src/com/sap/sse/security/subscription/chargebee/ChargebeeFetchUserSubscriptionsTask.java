package com.sap.sse.security.subscription.chargebee;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.subscription.SubscriptionRequestManagementService;

public class ChargebeeFetchUserSubscriptionsTask implements ChargebeeSubscriptionListRequest.OnResultListener {
    private static final Logger logger = Logger.getLogger(ChargebeeFetchUserSubscriptionsTask.class.getName());

    @FunctionalInterface
    public static interface OnResultListener {
        void onSubsctiptionsResult(Iterable<Subscription> subscriptions);
    }

    private final User user;
    private final SubscriptionRequestManagementService requestManagementService;
    private final OnResultListener listener;

    private List<Subscription> userSubscriptions;
    private String offset;

    public ChargebeeFetchUserSubscriptionsTask(User user, SubscriptionRequestManagementService requestManagementService,
            OnResultListener listener) {
        this.user = user;
        this.requestManagementService = requestManagementService;
        this.listener = listener;
    }

    public void run() {
        logger.info(() -> "Schedule fetch Chargebee subscriptions, user: " + user.getName() + ", offset: "
                + (offset == null ? "" : offset));
        requestManagementService.scheduleRequest(new ChargebeeSubscriptionListRequest(user, offset, this),
                ChargebeeApiService.TIME_FOR_API_REQUEST_MS, ChargebeeApiService.LIMIT_REACHED_RESUME_DELAY_MS);
    }

    @Override
    public void onSubscriptionListResult(Iterable<ChargebeeApiSubscriptionData> subscriptions, String nextOffset) {
        offset = nextOffset;
        if (subscriptions != null) {
            if (userSubscriptions == null) {
                userSubscriptions = new ArrayList<Subscription>();
            }
            for (ChargebeeApiSubscriptionData sub : subscriptions) {
                userSubscriptions.add(sub.toSubscription());
            }
        }
        if (offset == null || offset.isEmpty()) {
            if (listener != null) {
                listener.onSubsctiptionsResult(userSubscriptions);
            }
        } else {
            run();
        }
    }
}
