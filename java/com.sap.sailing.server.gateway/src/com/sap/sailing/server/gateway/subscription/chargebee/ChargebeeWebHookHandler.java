package com.sap.sailing.server.gateway.subscription.chargebee;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.sap.sailing.server.gateway.subscription.SubscriptionWebHookHandler;
import com.sap.sailing.server.gateway.subscription.SubscriptionWebHookServlet;
import com.sap.sse.common.Util.Pair;
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
        SubscriptionWebHookEvent event = null;
        try {
            event = (SubscriptionWebHookEvent) request.getAttribute("event");
            final User user = getUser(event.getCustomerId());
            if (user != null && !isOutdatedEvent(event, user)) {
                processEvent(event, user);
            }
            sendSuccess(response);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to proccess Chargebee subscription webhook event "
                    + (event != null ? event.getEventType().getName() : ""), e);
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
        if (isOutdated) {
            logger.log(Level.INFO, "Webhook event is outdated and won't be processed");
        }
        return isOutdated;
    }

    private boolean isOutdatedEventTime(long occuredAt, Subscription subscription) {
        return occuredAt < subscription.getLatestEventTime() || occuredAt < subscription.getManualUpdatedAt();
    }

    private void processEvent(SubscriptionWebHookEvent event, User user) throws UserManagementException {
        final SubscriptionWebHookEventType eventType = event.getEventType();
        if (eventType != null) {
            logger.log(Level.INFO,
                    "Start process webhook event \"" + eventType.getName() + "\" for user " + user.getName());
            final Subscription userSubscription = getCurrentUserSubscriptionFromEvent(user, event);
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
            case SUBSCRIPTION_PAUSED:
            case SUBSCRIPTION_RESUMED:
                updateUserSubscription(user, buildSubscription(userSubscription, event));
                break;
            case PAYMENT_REFUNDED:
                if (userSubscription.getInvoiceId() != null
                        && userSubscription.getInvoiceId().equals(event.getInvoiceId())) {
                    updateUserSubscription(user, buildSubscription(userSubscription, event));
                }
                break;
            case INVOICE_GENERATED:
                updateSubscriptionInvoice(user, userSubscription, event);
                break;
            case INVOICE_UPDATED:
                if (userSubscription != null && userSubscription.getInvoiceId() != null
                        && userSubscription.getInvoiceId().equals(event.getInvoiceId())) {
                    updateSubscriptionInvoice(user, userSubscription, event);
                }
                break;
            }
            logger.log(Level.INFO,
                    "Webhook event \"" + eventType.getName() + "\" has been processed for user " + user.getName());
        }
    }

    private Subscription getCurrentUserSubscriptionFromEvent(User user, SubscriptionWebHookEvent event) {
        final Subscription userSubscription;
        if (StringUtils.isNotEmpty(event.getPlanId())) {
            userSubscription = user.getSubscriptionByPlan(event.getPlanId());
        } else {
            userSubscription = user.getSubscriptionById(event.getSubscriptionId());
        }
        return userSubscription;
    }

    private Subscription buildEmptySubscription(Subscription currentSubscription, SubscriptionWebHookEvent event) {
        return ChargebeeSubscription.createEmptySubscription(event.getPlanId(), event.getEventOccurredAt(),
                currentSubscription != null ? currentSubscription.getManualUpdatedAt() : 0);
    }

    /**
     * Build new {@code Subscription} instance from current user subscription and webhook event
     * {@code SubscriptionWebHookEvent}
     */
    private Subscription buildSubscription(Subscription currentSubscription, SubscriptionWebHookEvent event) {
        String paymentStatus = null;
        String subscriptionStatus = event.getSubscriptionStatus();
        String transactionType = null;
        String transactionStatus = null;
        String invoiceId = null;
        String invoiceStatus = null;
        if (subscriptionStatus != null) {
            transactionType = event.getTransactionType();
            if (transactionType == null && currentSubscription != null) {
                transactionType = currentSubscription.getTransactionType();
            }
            transactionStatus = event.getTransactionStatus();
            if (transactionStatus == null && currentSubscription != null) {
                transactionStatus = currentSubscription.getTransactionStatus();
            }
            Pair<String, String> invoice = getInvoiceData(currentSubscription, event);
            invoiceId = invoice.getA();
            invoiceStatus = invoice.getB();
            paymentStatus = getEventPaymentStatus(event);
            if (paymentStatus == null && currentSubscription != null) {
                paymentStatus = currentSubscription.getPaymentStatus();
            }
        }
        return new ChargebeeSubscription(event.getSubscriptionId(), event.getPlanId(), event.getCustomerId(),
                event.getSubscriptionTrialStart(), event.getSubscriptionTrialEnd(), subscriptionStatus, paymentStatus,
                transactionType, transactionStatus, invoiceId, invoiceStatus, event.getSubscriptionCreatedAt(),
                event.getSubscriptionUpdatedAt(), event.getEventOccurredAt(),
                currentSubscription != null ? currentSubscription.getManualUpdatedAt() : 0);
    }

    /**
     * Update user subscription from invoice webhook events {@code SubscriptionWebHookEventType#INVOICE_GENERATED}
     * {@code SubscriptionWebHookEventType#INVOICE_UPDATED}
     * 
     * @param currentSubscription
     *            current user subscription
     * @param event
     *            webhook event
     * @throws UserManagementException
     */
    private void updateSubscriptionInvoice(User user, Subscription currentSubscription, SubscriptionWebHookEvent event)
            throws UserManagementException {
        if (currentSubscription != null && StringUtils.isNotEmpty(currentSubscription.getSubscriptionId())
                && currentSubscription.getSubscriptionId().equals(event.getInvoiceSubscriptionId())
                && StringUtils.isNotEmpty(currentSubscription.getCustomerId())
                && currentSubscription.getCustomerId().equals(event.getInvoiceCustomerId())) {
            Pair<String, String> invoice = getInvoiceData(null, event);
            String invoiceId = invoice.getA();
            String invoiceStatus = invoice.getB();
            String paymentStatus = determinePaymentStatusFromInvoiceStatus(invoiceStatus);
            Subscription newSubscription = new ChargebeeSubscription(currentSubscription.getSubscriptionId(),
                    currentSubscription.getPlanId(), currentSubscription.getCustomerId(),
                    currentSubscription.getTrialStart(), currentSubscription.getTrialEnd(),
                    currentSubscription.getSubscriptionStatus(), paymentStatus,
                    currentSubscription.getTransactionType(), currentSubscription.getTransactionStatus(), invoiceId,
                    invoiceStatus, currentSubscription.getSubscriptionCreatedAt(),
                    currentSubscription.getSubscriptionUpdatedAt(), event.getEventOccurredAt(),
                    currentSubscription.getManualUpdatedAt());
            updateUserSubscription(user, newSubscription);
        }
    }

    /**
     * Get subscription invoice id and invoice status. If the data do not exist in webhook event then they will get
     * value from user current subscription if it's not null
     * 
     * @param currentSubscription
     *            current user subscription
     * @param event
     *            webhook event
     * @return subscription invoice id and invoice status
     */
    private Pair<String, String> getInvoiceData(Subscription currentSubscription, SubscriptionWebHookEvent event) {
        String invoiceId = event.getInvoiceId();
        if (invoiceId == null && currentSubscription != null) {
            invoiceId = currentSubscription.getInvoiceId();
        }
        String invoiceStatus = event.getInvoiceStatus();
        if (invoiceStatus == null && currentSubscription != null) {
            invoiceStatus = currentSubscription.getInvoiceStatus();
        }
        return new Pair<String, String>(invoiceId, invoiceStatus);
    }

    /**
     * Determine payment status value for {@code Subscription}
     */
    private String getEventPaymentStatus(SubscriptionWebHookEvent event) {
        String paymentStatus = null;
        String transactionStatus = event.getTransactionStatus();
        if (transactionStatus == null) {
            String invoiceStatus = event.getInvoiceStatus();
            if (invoiceStatus != null) {
                paymentStatus = determinePaymentStatusFromInvoiceStatus(invoiceStatus);
            }
        } else {
            String transactionType = event.getTransactionType();
            if (transactionType != null && transactionType.equals(ChargebeeSubscription.TRANSACTION_TYPE_PAYMENT)) {
                paymentStatus = determinePaymentStatusFromTransactionStatus(transactionStatus);
            }
        }
        return paymentStatus;
    }

    /**
     * If invoice is paid then payment status will be {@code Subscription#PAYMENT_STATUS_SUCCESS}, and
     * {@code Subscription#PAYMENT_STATUS_NO_SUCCESS} otherwise
     * 
     * @param invoiceStatus
     *            event invoice status
     */
    private String determinePaymentStatusFromInvoiceStatus(String invoiceStatus) {
        return invoiceStatus.equals(SubscriptionWebHookEvent.INVOICE_STATUS_PAID) ? Subscription.PAYMENT_STATUS_SUCCESS
                : Subscription.PAYMENT_STATUS_NO_SUCCESS;
    }

    /**
     * If transaction is success then payment status will be {@code Subscription#PAYMENT_STATUS_SUCCESS}, and
     * {@code Subscription#PAYMENT_STATUS_NO_SUCCESS} otherwise
     * 
     * @param transactionStatus
     *            event transaction status
     */
    private String determinePaymentStatusFromTransactionStatus(String transactionStatus) {
        return transactionStatus.equals(ChargebeeSubscription.TRANSACTION_STATUS_SUCCESS)
                ? Subscription.PAYMENT_STATUS_SUCCESS
                : Subscription.PAYMENT_STATUS_NO_SUCCESS;
    }
}
