package com.sap.sse.security.subscription.chargebee;

import java.sql.Timestamp;

import com.chargebee.models.Invoice;
import com.chargebee.models.ItemPrice;
import com.chargebee.models.Subscription;
import com.chargebee.models.Subscription.SubscriptionItem;
import com.chargebee.models.Subscription.SubscriptionItem.ItemType;
import com.chargebee.models.Transaction;
import com.sap.sse.common.TimePoint;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscription;

public class ChargebeeApiSubscriptionData {
    private final Subscription subscription;
    private final Invoice invoice;
    private final Transaction transaction;

    public ChargebeeApiSubscriptionData(Subscription subscription, Invoice invoice,
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
        final Timestamp trialStart = subscription.trialStart();
        final Timestamp trialEnd = subscription.trialEnd();
         String planId = getPlanId();
         return new ChargebeeSubscription(subscription.id(), planId, subscription.customerId(),
                 trialStart == null ? com.sap.sse.security.shared.subscription.Subscription.emptyTime()
                         : TimePoint.of(trialStart),
                 trialStart == null ? com.sap.sse.security.shared.subscription.Subscription.emptyTime()
                         : TimePoint.of(trialEnd),
                 subscriptionStatus, paymentStatus, transactionType, transactionStatus, invoiceId, invoiceStatus,
                 TimePoint.of(subscription.createdAt()), TimePoint.of(subscription.updatedAt()), TimePoint.now(),
                 com.sap.sse.security.shared.subscription.Subscription.emptyTime());
    }
    
    // TODO bug5510 Integrate this into the API Request / APIService Structure to ensure the API limits are kept
    private String getPlanId() {
        for(SubscriptionItem item : subscription.subscriptionItems()) {
            if(item.itemType().equals(ItemType.PLAN)) {
                final String itemPriceId = item.itemPriceId();
                try {
                    final ItemPrice itemPrice = ItemPrice.retrieve(itemPriceId).request().itemPrice();
                    return itemPrice.itemId();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }

    private String stringToLowerCase(String str) {
        return str == null ? null : str.toLowerCase();
    }
}
