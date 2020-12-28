package com.sap.sse.security.subscription.chargebee;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.chargebee.ListResult;
import com.chargebee.ListResult.Entry;
import com.chargebee.filters.enums.SortOrder;
import com.chargebee.models.Invoice;
import com.chargebee.models.Subscription;
import com.chargebee.models.Transaction;
import com.chargebee.models.Subscription.SubscriptionListRequest;
import com.sap.sse.security.shared.impl.User;

public class ChargebeeSubscriptionListRequest extends ChargebeeApiRequest
        implements ChargebeeInvoiceRequest.OnResultListener, ChargebeeTransactionRequest.OnResultListener {

    private static final Logger logger = Logger.getLogger(ChargebeeSubscriptionListRequest.class.getName());

    public static interface OnResultListener {
        void onSubscriptionListResult(Iterable<ChargebeeApiSubscriptionData> subscriptions, String nextOffset);
    }

    private User user;
    private String offset;
    private OnResultListener listener;
    private Iterator<Entry> resultIterator;
    private List<ChargebeeApiSubscriptionData> subscriptions;
    private String nextOffset;

    private Subscription subscription;
    private Invoice invoice;

    public ChargebeeSubscriptionListRequest(User user, String offset, OnResultListener listener) {
        this.user = user;
        this.offset = offset;
        this.listener = listener;
    }

    @Override
    public void run() {
        logger.info(() -> "Fetching subscription list, user: " + user.getName() + ", offset: "
                + (offset == null ? "" : offset));
        SubscriptionListRequest request = Subscription.list().limit(100).customerId().is(user.getName())
                .includeDeleted(false).sortByCreatedAt(SortOrder.DESC);
        if (offset != null && !offset.isEmpty()) {
            request.offset(offset);
        }
        try {
            ListResult result = request.request();
            if (!isRateLimitReached(result)) {
                if (result != null && !result.isEmpty()) {
                    resultIterator = result.iterator();
                    subscriptions = new ArrayList<ChargebeeApiSubscriptionData>();
                    nextOffset = result.nextOffset();
                    processEntry();
                } else {
                    onDone(null, null);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fetching subscription list failed, user: " + user.getName() + ", offset: "
                    + (offset == null ? "" : offset));
            onDone(null, null);
        }
    }

    @Override
    public void onInvoiceResult(Invoice invoice) {
        logger.info(() -> "Invoice result: " + (invoice != null ? invoice.toJson() : "empty"));
        this.invoice = invoice;
        getRequestManagementService().scheduleRequest(new ChargebeeTransactionRequest(user, subscription.id(), this),
                ChargebeeApiService.TIME_FOR_API_REQUEST_MS, ChargebeeApiService.LIMIT_REACHED_RESUME_DELAY_MS);
    }

    @Override
    public void onTransactionResult(Transaction transaction) {
        logger.info(() -> "Transaction result: " + (transaction != null ? transaction.toJson() : "empty"));
        subscriptions.add(new ChargebeeApiSubscriptionData(subscription, invoice, transaction));

        processEntry();
    }

    private void processEntry() {
        if (!resultIterator.hasNext()) {
            onDone(subscriptions, nextOffset);
        } else {
            ListResult.Entry entry = resultIterator.next();
            subscription = entry.subscription();
            if (subscription != null) {
                if (!subscription.deleted()) {
                    getRequestManagementService().scheduleRequest(
                            new ChargebeeInvoiceRequest(user, subscription.id(), this),
                            ChargebeeApiService.TIME_FOR_API_REQUEST_MS,
                            ChargebeeApiService.LIMIT_REACHED_RESUME_DELAY_MS);
                } else {
                    subscriptions.add(new ChargebeeApiSubscriptionData(subscription, null, null));
                    processEntry();
                }
            } else {
                processEntry();
            }
        }
    }

    private void onDone(Iterable<ChargebeeApiSubscriptionData> subscriptions, String nextOffset) {
        if (listener != null) {
            listener.onSubscriptionListResult(subscriptions, nextOffset);
        }
    }
}
