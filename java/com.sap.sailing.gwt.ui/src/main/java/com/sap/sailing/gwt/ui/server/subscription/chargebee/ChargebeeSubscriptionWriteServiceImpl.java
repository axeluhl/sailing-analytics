package com.sap.sailing.gwt.ui.server.subscription.chargebee;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.chargebee.Result;
import com.chargebee.models.HostedPage;
import com.chargebee.models.Invoice;
import com.chargebee.models.Transaction;
import com.chargebee.models.HostedPage.Content;
import com.sap.sailing.gwt.ui.client.subscription.chargebee.ChargebeeSubscriptionWriteService;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.FinishCheckoutDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscription;
import com.sap.sse.security.subscription.SubscriptionCancelResult;

public class ChargebeeSubscriptionWriteServiceImpl extends ChargebeeSubscriptionServiceImpl
        implements ChargebeeSubscriptionWriteService {
    
    private static final long serialVersionUID = 3058555834123504387L;
    
    private static final Logger logger = Logger.getLogger(ChargebeeSubscriptionWriteServiceImpl.class.getName());

    @Override
    public SubscriptionDTO finishCheckout(String planId, FinishCheckoutDTO data) {
        logger.info("finishCheckout hostedPageId: " + data.getHostedPageId());
        SubscriptionDTO subscriptionDto;
        try {
            User user = getCurrentUser();
            Result result = HostedPage.acknowledge(data.getHostedPageId()).request();
            Content content = result.hostedPage().content();
            String transactionType = null;
            String transactionStatus = null;
            Transaction transaction = content.transaction();
            if (transaction != null) {
                transactionType = transaction.type().name().toLowerCase();
                transactionStatus = transaction.status().name().toLowerCase();
            }
            Invoice invoice = content.invoice();
            String invoiceId = null;
            String invoiceStatus = null;
            if (invoice != null) {
                invoiceId = invoice.id();
                invoiceStatus = invoice.status().name().toLowerCase();
            }
            Subscription subscription = new ChargebeeSubscription(content.subscription().id(),
                    content.subscription().planId(), content.customer().id(),
                    TimePoint.of(content.subscription().trialStart()), TimePoint.of(content.subscription().trialEnd()),
                    content.subscription().status().name().toLowerCase(), null, transactionType, transactionStatus,
                    invoiceId, invoiceStatus, TimePoint.of(content.subscription().createdAt()),
                    TimePoint.of(content.subscription().updatedAt()), Subscription.emptyTime(), TimePoint.now());
            updateUserSubscription(user, subscription);
            subscriptionDto = getSubscription();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in saving subscription", e);
            subscriptionDto = new SubscriptionDTO(null, e.getMessage());
        }
        return subscriptionDto;
    }

    @Override
    public boolean cancelSubscription(String planId) {
        boolean result;
        try {
            User user = getCurrentUser();
            Subscription subscription = user.getSubscriptionByPlan(planId);
            if (isValidSubscription(subscription)) {
                logger.info(() -> "Cancel user subscription, user " + user.getName() + ", plan " + planId);
                SubscriptionCancelResult cancelResult = getApiService()
                        .cancelSubscription(subscription.getSubscriptionId());
                if (cancelResult.isSuccess()) {
                    logger.info(() -> "Cancel subscription successful");
                    result = true;
                    if (cancelResult.getSubscription() != null) {
                        updateUserSubscription(user, cancelResult.getSubscription());
                    }
                } else {
                    result = false;
                    if (cancelResult.isDeleted()) {
                        logger.info(() -> "Subscription for plan was deleted");
                        Subscription emptySubscription = ChargebeeSubscription.createEmptySubscription(planId,
                                subscription.getLatestEventTime(), TimePoint.now());
                        updateUserSubscription(user, emptySubscription);
                    } else {
                        logger.info(() -> "Cancel subscription failed");
                    }
                }
            } else {
                logger.info(() -> "Invalid subscription");
                result = false;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in cancel subscription ", e);
            result = false;
        }
        return result;
    }
}
