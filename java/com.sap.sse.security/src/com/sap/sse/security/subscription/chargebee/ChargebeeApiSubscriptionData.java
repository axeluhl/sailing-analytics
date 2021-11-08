package com.sap.sse.security.subscription.chargebee;

import com.chargebee.models.Invoice;
import com.chargebee.models.Subscription;
import com.chargebee.models.Transaction;
import com.sap.sse.common.TimePoint;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscription;

public class ChargebeeApiSubscriptionData {
    private final Subscription subscription;
    private final Invoice invoice;
    private final Transaction transaction;

    public ChargebeeApiSubscriptionData(com.chargebee.models.Subscription subscription, Invoice invoice,
            Transaction transaction) {
        this.subscription = subscription;
        this.invoice = invoice;
        this.transaction = transaction;
    }

    public ChargebeeSubscription toSubscription() {
        String subscriptionStatus = null;
        if (subscription.status() != null) {
            subscriptionStatus = stringToLowerCase(subscription.status().name());
        }
        String transactionType = null;
        String transactionStatus = null;
        if (transaction != null) {
            transactionType = stringToLowerCase(transaction.type().name());
            transactionStatus = stringToLowerCase(transaction.status().name());
        }
        String invoiceId = null;
        String invoiceStatus = null;
        if (invoice != null) {
            invoiceId = invoice.id();
            invoiceStatus = stringToLowerCase(invoice.status().name());
        }
        String paymentStatus = ChargebeeSubscription.determinePaymentStatus(transactionType, transactionStatus,
                invoiceStatus);
        return new ChargebeeSubscription(subscription.id(), subscription.planId(), subscription.customerId(),
                TimePoint.of(subscription.trialStart()), TimePoint.of(subscription.trialEnd()), subscriptionStatus,
                paymentStatus, transactionType, transactionStatus, invoiceId, invoiceStatus,
                TimePoint.of(subscription.createdAt()), TimePoint.of(subscription.updatedAt()), TimePoint.now(),
                TimePoint.now());
    }

    private String stringToLowerCase(String str) {
        return str == null ? null : str.toLowerCase();
    }
}
