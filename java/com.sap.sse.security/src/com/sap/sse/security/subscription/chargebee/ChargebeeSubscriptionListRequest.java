package com.sap.sse.security.subscription.chargebee;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.chargebee.ListResult;
import com.chargebee.models.Invoice;
import com.chargebee.models.Subscription;
import com.chargebee.models.Subscription.SubscriptionListRequest;
import com.chargebee.models.Transaction;
import com.sap.sse.security.subscription.SubscriptionApiBaseService;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;

/**
 * Fetch user subscription list page (by offset), as well as each subscription invoice and transaction data. Results
 * will be notified by {@code ChargebeeSubscriptionListRequest.OnResultListener}
 */
public class ChargebeeSubscriptionListRequest extends ChargebeeApiRequest {

    private static final Logger logger = Logger.getLogger(ChargebeeSubscriptionListRequest.class.getName());

    @FunctionalInterface
    public static interface OnResultListener {
        void onSubscriptionListResult(Iterable<ChargebeeApiSubscriptionData> subscriptions, String nextOffset);
    }

    private final OnResultListener listener;
    private final String offset;
    private List<ChargebeeApiSubscriptionData> subscriptions;
    private String nextOffset;
    private int resultSize;

    public ChargebeeSubscriptionListRequest(String offset, OnResultListener listener,
            SubscriptionApiRequestProcessor requestProcessor, SubscriptionApiBaseService chargebeeApiServiceParams) {
        super(requestProcessor, chargebeeApiServiceParams);
        this.offset = offset;
        this.listener = listener;
    }

    @Override
    protected ChargebeeInternalApiRequestWrapper createRequest() {
        SubscriptionListRequest request = Subscription.list()
                .limit(100).includeDeleted(false);
        if (offset != null && !offset.isEmpty()) {
            request.offset(offset);
        }
        return new ChargebeeInternalApiRequestWrapper(request);
    }

    @Override
    protected void processResult(ChargebeeInternalApiRequestWrapper request) {
        ListResult result = request.getListResult();
        if (result != null) {
            resultSize = result.size();
            nextOffset = result.nextOffset();
            processListResult(result);
        } else {
            onDone(null, null);
        }
    }

    @Override
    protected void handleError(Exception e) {
        logger.log(Level.SEVERE, "Fetching subscription list failed, offset: "
                + (offset == null ? "" : offset), e);
        onDone(null, null);
    }

    private void processListResult(ListResult result) {
        subscriptions = new ArrayList<ChargebeeApiSubscriptionData>();
        for (ListResult.Entry entry : result) {
            final Subscription subscription = entry.subscription();
            final Invoice invoice = entry.invoice();
            final Transaction transaction = entry.transaction();
            subscriptions.add(new ChargebeeApiSubscriptionData(subscription, invoice, transaction));
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
