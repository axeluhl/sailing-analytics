package com.sap.sse.security.subscription.chargebee;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.chargebee.ListResult;
import com.chargebee.filters.enums.SortOrder;
import com.chargebee.models.Subscription;
import com.chargebee.models.Subscription.SubscriptionListRequest;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.subscription.SubscriptionApiBaseService;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;

/**
 * Fetch user subscription list page (by offset), as well as each subscription invoice and transaction data. Results
 * will be notified by {@code ChargebeeSubscriptionListRequest.OnResultListener}
 */
public class ChargebeeSubscriptionListRequest extends ChargebeeApiRequest
        implements ChargebeeFetchSubscriptionInformationTask.OnResultListener {

    private static final Logger logger = Logger.getLogger(ChargebeeSubscriptionListRequest.class.getName());

    @FunctionalInterface
    public static interface OnResultListener {
        void onSubscriptionListResult(Iterable<ChargebeeApiSubscriptionData> subscriptions, String nextOffset);
    }

    private final User user;
    private final OnResultListener listener;
    private final String offset;

    private List<ChargebeeApiSubscriptionData> subscriptions;
    private String nextOffset;
    private int resultSize;

    public ChargebeeSubscriptionListRequest(User user, String offset, OnResultListener listener,
            SubscriptionApiRequestProcessor requestProcessor, SubscriptionApiBaseService chargebeeApiServiceParams) {
        super(requestProcessor, chargebeeApiServiceParams);
        this.user = user;
        this.offset = offset;
        this.listener = listener;
    }

    @Override
    public void onSubscriptionDataResult(String subscriptionId, ChargebeeApiSubscriptionData subscription) {
        subscriptions.add(subscription);
        if (subscriptions.size() == resultSize) {
            onDone(subscriptions, nextOffset);
        }
    }

    @Override
    protected ChargebeeInternalApiRequestWrapper createRequest() {
        logger.info(() -> "Fetching subscription list, user: " + user.getName() + ", offset: "
                + (offset == null ? "" : offset));
        SubscriptionListRequest request = Subscription.list().limit(100).customerId().is(user.getName())
                .includeDeleted(false).sortByCreatedAt(SortOrder.DESC);
        if (offset != null && !offset.isEmpty()) {
            request.offset(offset);
        }
        return new ChargebeeInternalApiRequestWrapper(request);
    }

    @Override
    protected void processResult(ChargebeeInternalApiRequestWrapper request) {
        ListResult result = request.getListResult();
        if (result != null && !result.isEmpty()) {
            resultSize = result.size();
            nextOffset = result.nextOffset();
            processListResult(result);
        } else {
            onDone(null, null);
        }
    }

    @Override
    protected void handleError(Exception e) {
        logger.log(Level.SEVERE, "Fetching subscription list failed, user: " + user.getName() + ", offset: "
                + (offset == null ? "" : offset));
        onDone(null, null);
    }

    private void processListResult(ListResult result) {
        subscriptions = new ArrayList<ChargebeeApiSubscriptionData>();
        for (ListResult.Entry entry : result) {
            Subscription subscription = entry.subscription();
            if (!subscription.deleted()) {
                new ChargebeeFetchSubscriptionInformationTask(user, subscription, this, getRequestProcessor(), getSubscriptionApiBaseService()).run();
            } else {
                subscriptions.add(new ChargebeeApiSubscriptionData(subscription, null, null));
            }
        }
        if (subscriptions.size() == resultSize) {
            onDone(subscriptions, nextOffset);
        }
    }

    private void onDone(Iterable<ChargebeeApiSubscriptionData> subscriptions, String nextOffset) {
        if (listener != null) {
            listener.onSubscriptionListResult(subscriptions, nextOffset);
        }
    }
}
