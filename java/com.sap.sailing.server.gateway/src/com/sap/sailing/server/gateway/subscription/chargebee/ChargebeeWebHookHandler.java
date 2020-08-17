package com.sap.sailing.server.gateway.subscription.chargebee;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.sap.sailing.server.gateway.subscription.SubscriptionWebHookHandler;
import com.sap.sailing.server.gateway.subscription.SubscriptionWebHookServlet;
import com.sap.sse.security.shared.Subscription;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.impl.ChargebeeSubscription;
import com.sap.sse.security.shared.impl.User;

/**
 * Servlet for handling WebHook events, response with status 200 in success handling
 * 
 * {@link https://www.chargebee.com/docs/events_and_webhooks.html}
 * 
 * @author Tu Tran
 */
public class ChargebeeWebHookHandler extends SubscriptionWebHookHandler {
    private static final Logger logger = Logger.getLogger(ChargebeeWebHookHandler.class.getName());

    public ChargebeeWebHookHandler(SubscriptionWebHookServlet context) {
        super(context);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) {
        try {
            final SubscriptionWebHookEvent event = (SubscriptionWebHookEvent) request.getAttribute("event");
            final User user = getUser(event.getCustomerId());
            if (user != null && !isOutdatedEvent(event, user)) {
                processEvent(event, user);
            }
            sendSuccess(response);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to proccess subscription webhook event", e);
            sendFail(response);
        }
    }

    /**
     * 
     * @return {@code true} if event occurred at time is less than latest user subscription updated time
     */
    private boolean isOutdatedEvent(SubscriptionWebHookEvent event, User user) {
        final long occuredAt = event.getEventOccurredAt();
        final String planId = event.getPlanId();
        boolean isOutdated = false;
        if (StringUtils.isNotEmpty(planId)) {
            Subscription subscription = user.getSubscriptionByPlan(planId);
            if (subscription != null) {
                isOutdated = isOutdatedEventTime(occuredAt, subscription);
            } else {
                Subscription[] subscriptions = user.getSubscriptions();
                if (subscriptions != null && subscriptions.length > 0) {
                    for (Subscription sub : subscriptions) {
                        if (!sub.hasPlan() && isOutdatedEventTime(occuredAt, sub)) {
                            isOutdated = true;
                            break;
                        }
                    }
                }
            }
        } else {
            Subscription[] subscriptions = user.getSubscriptions();
            if (subscriptions != null && subscriptions.length > 0) {
                for (Subscription subscription : subscriptions) {
                    if (isOutdatedEventTime(occuredAt, subscription)) {
                        isOutdated = true;
                        break;
                    }
                }
            }
        }
        return isOutdated;
    }

    private boolean isOutdatedEventTime(long occuredAt, Subscription subscription) {
        return occuredAt < subscription.getLatestEventTime() || occuredAt < subscription.getManuallyUpdatedAt();
    }

    private void processEvent(SubscriptionWebHookEvent event, User user) throws UserManagementException {
        final SubscriptionWebHookEventType eventType = event.getEventType();
        if (eventType != null) {
            final Subscription userSubscription = user.getSubscriptionByPlan(event.getPlanId());
            switch (eventType) {
            case CUSTOMER_DELETED:
                updateUserSubscription(user, buildEmptySubscription(userSubscription, event));
                break;
            case SUBSCRIPTION_CANCELLED:
            case SUBSCRIPTION_DELETED:
                if (userSubscription != null && userSubscription.getSubscriptionId() != null
                        && userSubscription.getSubscriptionId().equals(event.getSubscriptionId())) {
                    updateUserSubscription(user, buildEmptySubscription(userSubscription, event));
                }
                break;
            case SUBSCRIPTION_CREATED:
            case SUBSCRIPTION_CHANGED:
            case SUBSCRIPTION_ACTIVATED:
            case PAYMENT_SUCCEEDED:
            case PAYMENT_FAILED:
                updateUserSubscription(user, buildSubscription(userSubscription, event));
                break;
            }
        }
    }

    private Subscription buildEmptySubscription(Subscription currentSubscription, SubscriptionWebHookEvent event) {
        return ChargebeeSubscription.createEmptySubscription(event.getPlanId(), event.getEventOccurredAt(),
                currentSubscription != null ? currentSubscription.getManuallyUpdatedAt() : 0);
    }

    private Subscription buildSubscription(Subscription currentSubscription, SubscriptionWebHookEvent event) {
        String paymentStatus = null;
        String subscriptionStatus = event.getSubscriptionStatus();
        if (subscriptionStatus != null
                && subscriptionStatus.equals(SubscriptionWebHookEvent.SUBSCRIPTION_STATUS_ACTIVE)) {
            paymentStatus = getEventPaymentStatus(event);
            if (paymentStatus == null && currentSubscription != null) {
                paymentStatus = currentSubscription.getPaymentStatus();
            }
        }
        return new ChargebeeSubscription(event.getSubscriptionId(), event.getPlanId(), event.getCustomerId(),
                event.getSubscriptionTrialStart(), event.getSubscriptionTrialEnd(), subscriptionStatus, paymentStatus,
                event.getSubscriptionCreatedAt(), event.getSubscriptionUpdatedAt(), event.getEventOccurredAt(),
                currentSubscription != null ? currentSubscription.getManuallyUpdatedAt() : 0);
    }

    private String getEventPaymentStatus(SubscriptionWebHookEvent event) {
        String paymentStatus = null;
        String transactionStatus = event.getTransactionStatus();
        if (transactionStatus == null) {
            String invoiceStatus = event.getInvoiceStatus();
            if (invoiceStatus != null) {
                paymentStatus = invoiceStatus.equals(SubscriptionWebHookEvent.INVOICE_STATUS_PAID)
                        ? Subscription.PAYMENT_STATUS_SUCCESS
                        : Subscription.PAYMENT_STATUS_NO_SUCCESS;
            }
        } else {
            String transactionType = event.getTransactionType();
            if (transactionType != null && transactionType.equals(SubscriptionWebHookEvent.TRANSACTION_TYPE_PAYMENT)) {
                paymentStatus = transactionStatus.equals(SubscriptionWebHookEvent.TRANSACTION_STATUS_SUCCESS)
                        ? Subscription.PAYMENT_STATUS_SUCCESS
                        : Subscription.PAYMENT_STATUS_NO_SUCCESS;
            }
        }

        return paymentStatus;
    }
}
