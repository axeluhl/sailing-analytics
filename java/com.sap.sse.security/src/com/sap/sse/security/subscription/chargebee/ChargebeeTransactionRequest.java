package com.sap.sse.security.subscription.chargebee;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.chargebee.ListResult;
import com.chargebee.filters.enums.SortOrder;
import com.chargebee.models.Transaction;
import com.chargebee.models.Transaction.TransactionListRequest;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;

/**
 * Fetch Chargebee subscription transaction request
 */
public class ChargebeeTransactionRequest extends ChargebeeApiRequest {
    private static final Logger logger = Logger.getLogger(ChargebeeTransactionRequest.class.getName());

    public static interface OnResultListener {
        void onTransactionResult(String subscriptionId, Transaction transaction);
    }

    private final User user;
    private final String subscriptionId;
    private final OnResultListener listener;

    public ChargebeeTransactionRequest(User user, String subscriptionId, OnResultListener listener,
            SubscriptionApiRequestProcessor requestProcessor) {
        super(requestProcessor);
        this.user = user;
        this.subscriptionId = subscriptionId;
        this.listener = listener;
    }

    @Override
    protected ChargebeeInternalApiRequestWrapper createRequest() {
        logger.info(() -> "Fetch Chargebee transaction, user: " + user.getName() + ", subscription " + subscriptionId);

        TransactionListRequest request = Transaction.list().limit(1).subscriptionId().is(subscriptionId).customerId()
                .is(user.getName()).sortByDate(SortOrder.DESC);
        return new ChargebeeInternalApiRequestWrapper(request);
    }

    @Override
    protected void processResult(ChargebeeInternalApiRequestWrapper request) {
        ListResult result = request.getListResult();
        final Transaction transaction;
        if (result != null && !result.isEmpty()) {
            transaction = result.get(0).transaction();
        } else {
            transaction = null;
        }
        onDone(transaction);
    }

    @Override
    protected void handleError(Exception e) {
        logger.log(Level.SEVERE,
                "Fetch Chargebee transaction failed, user: " + user.getName() + ", subscription " + subscriptionId, e);
        onDone(null);
    }

    private void onDone(Transaction transaction) {
        if (listener != null) {
            listener.onTransactionResult(subscriptionId, transaction);
        }
    }
}
