package com.sap.sse.security.subscription.chargebee;

import java.util.logging.Logger;

import com.chargebee.models.Invoice;
import com.chargebee.models.Subscription;
import com.chargebee.models.Transaction;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;

/**
 * Fetch subscription invoice and transaction data. Result will be notified by
 * {@code ChargebeeFetchSubscriptionInformationTask.OnResultListener}
 */
public class ChargebeeFetchSubscriptionInformationTask
        implements ChargebeeInvoiceRequest.OnResultListener, ChargebeeTransactionRequest.OnResultListener {

    private static final Logger logger = Logger.getLogger(ChargebeeFetchSubscriptionInformationTask.class.getName());

    public static interface OnResultListener {
        void onSubscriptionDataResult(String subscriptionId, ChargebeeApiSubscriptionData subscription);
    }

    private final User user;
    private final Subscription subscription;
    private final OnResultListener listener;
    private final SubscriptionApiRequestProcessor requestProcessor;
    
    private boolean hasInvoice;
    private boolean hasTransaction;
    private Invoice invoice;
    private Transaction transaction;

    public ChargebeeFetchSubscriptionInformationTask(User user, Subscription subscription, OnResultListener listener,
            SubscriptionApiRequestProcessor requestProcessor) {
        this.user = user;
        this.subscription = subscription;
        this.listener = listener;
        this.requestProcessor = requestProcessor;
    }

    public void run() {
        logger.info(() -> "Fetch Chargebee subscription information, user: " + user.getName() + ", subscription "
                + subscription.id());

        requestProcessor.addRequest(new ChargebeeInvoiceRequest(user, subscription.id(), this, requestProcessor));
        requestProcessor.addRequest(new ChargebeeTransactionRequest(user, subscription.id(), this, requestProcessor));
    }

    @Override
    public void onTransactionResult(String subscriptionId, Transaction transaction) {
        hasTransaction = true;
        this.transaction = transaction;
        checkDone();
    }

    @Override
    public void onInvoiceResult(String subscriptionId, Invoice invoice) {
        hasInvoice = true;
        this.invoice = invoice;
        checkDone();
    }

    private void onDone() {
        if (listener != null) {
            listener.onSubscriptionDataResult(subscription.id(),
                    new ChargebeeApiSubscriptionData(subscription, invoice, transaction));
        }
    }

    private void checkDone() {
        if (hasInvoice && hasTransaction) {
            onDone();
        }
    }

}
