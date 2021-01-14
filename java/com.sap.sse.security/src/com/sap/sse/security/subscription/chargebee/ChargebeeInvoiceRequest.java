package com.sap.sse.security.subscription.chargebee;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.chargebee.ListResult;
import com.chargebee.filters.enums.SortOrder;
import com.chargebee.models.Invoice;
import com.chargebee.models.Invoice.InvoiceListRequest;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;

/**
 * Fetch Chargebee subscription invoice request
 */
public class ChargebeeInvoiceRequest extends ChargebeeApiRequest {
    private static final Logger logger = Logger.getLogger(ChargebeeInvoiceRequest.class.getName());

    /**
     * Invoice result listener
     */
    public static interface OnResultListener {
        void onInvoiceResult(String subscriptionId, Invoice invoice);
    }

    private User user;
    private String subscriptionId;
    private OnResultListener listener;
    private final SubscriptionApiRequestProcessor requestProcessor;

    public ChargebeeInvoiceRequest(User user, String subscriptionId, OnResultListener listener,
            SubscriptionApiRequestProcessor requestProcessor) {
        this.user = user;
        this.subscriptionId = subscriptionId;
        this.listener = listener;
        this.requestProcessor = requestProcessor;
    }

    @Override
    public void run() {
        logger.info(() -> "Fetch Chargebee invoice, user: " + user.getName() + ", subscription id: " + subscriptionId);
        InvoiceListRequest request = Invoice.list().limit(1).subscriptionId().is(subscriptionId).customerId()
                .is(user.getName()).sortByDate(SortOrder.DESC);
        try {
            ListResult result = request.request();
            if (!isRateLimitReached(result)) {
                final Invoice invoice;
                if (result != null && !result.isEmpty()) {
                    invoice = result.get(0).invoice();
                } else {
                    invoice = null;
                }
                onDone(invoice);
            } else {
                requestProcessor.addRequest(this, LIMIT_REACHED_RESUME_DELAY_MS);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,
                    "Fetch Chargebee invoice failed, user: " + user.getName() + ", subscription id: " + subscriptionId,
                    e);
            onDone(null);
        }
    }

    private void onDone(Invoice invoice) {
        if (listener != null) {
            listener.onInvoiceResult(subscriptionId, invoice);
        }
    }
}
