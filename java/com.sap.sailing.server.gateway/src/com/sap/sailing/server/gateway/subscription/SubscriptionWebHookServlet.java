package com.sap.sailing.server.gateway.subscription;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.sap.sailing.server.gateway.SailingServerHttpServlet;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sse.security.shared.Subscription;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.impl.User;

/**
 * Servlet for handling WebHook events, response with status 200 in success handling
 * 
 * {@link https://www.chargebee.com/docs/events_and_webhooks.html}
 * 
 * @author Tu Tran
 */
public class SubscriptionWebHookServlet extends SailingServerHttpServlet {
    private static final long serialVersionUID = 2608645647937414012L;
    private static final Logger logger = Logger.getLogger(SubscriptionWebHookServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            final Object requestBody = JSONValue.parseWithException(request.getReader());
            final JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);
            logger.log(Level.INFO, "Payment service webhook data: " + requestObject.toJSONString());
            final SubscriptionWebHookEvent event = new SubscriptionWebHookEvent(requestObject);
            if (!event.isValidEvent()) {
                throw new IllegalArgumentException("Invalid webhook event");
            }
            final User user = getUser(event.getCustomerId());
            if (user != null && !isOutdatedEvent(event, user.getSubscription())) {
                processEvent(event, user);
            }
            sendSuccess(response);
        } catch (ParseException e) {
            logger.log(Level.SEVERE, "Failed to parse subscription webhook event data", e);
            sendFail(response);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to proccess subscription webhook event", e);
            sendFail(response);
        }
    }

    private boolean isOutdatedEvent(SubscriptionWebHookEvent event, Subscription userSubscription) {
        final long occuredAt = event.getEventOccurredAt();
        return userSubscription != null && (occuredAt < userSubscription.getLatestEventTime()
                || occuredAt < userSubscription.getManuallyUpdatedAt());
    }

    private void processEvent(SubscriptionWebHookEvent event, User user) throws UserManagementException {
        final SubscriptionWebHookEventType eventType = event.getEventType();
        if (eventType != null) {
            final Subscription userSubscription = user.getSubscription();
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
        Subscription subscription = new Subscription();
        subscription.setLatestEventTime(event.getEventOccurredAt());
        subscription.setManualUpdatedAt(currentSubscription != null ? currentSubscription.getManuallyUpdatedAt() : 0);
        return subscription;
    }

    private Subscription buildSubscription(Subscription currentSubscription, SubscriptionWebHookEvent event) {
        Subscription subscription = new Subscription();
        subscription.setSubscriptionId(event.getSubscriptionId());
        subscription.setPlanId(event.getPlanId());
        subscription.setCustomerId(event.getCustomerId());
        subscription.setTrialStart(event.getSubscriptionTrialStart());
        subscription.setTrialEnd(event.getSubscriptionTrialEnd());
        subscription.setSubscriptionStatus(event.getSubscriptionStatus());
        subscription.setSubsciptionCreatedAt(event.getSubscriptionCreatedAt());
        subscription.setSubsciptionUpdatedAt(event.getSubscriptionUpdatedAt());
        subscription.setLatestEventTime(event.getEventOccurredAt());
        subscription.setManualUpdatedAt(currentSubscription != null ? currentSubscription.getManuallyUpdatedAt() : 0);
        if (subscription.getSubscriptionStatus() != null
                && subscription.getSubscriptionStatus().equals(SubscriptionWebHookEvent.SUBSCRIPTION_STATUS_ACTIVE)) {
            String paymentStatus = getEventPaymentStatus(event);
            if (paymentStatus != null) {
                subscription.setPaymentStatus(paymentStatus);
            } else {
                subscription
                        .setPaymentStatus(currentSubscription != null ? currentSubscription.getPaymentStatus() : null);
            }
        } else {
            subscription.setPaymentStatus(null);
        }
        return subscription;
    }

    private User getUser(String customerId) {
        return getSecurityService().getUserByName(customerId);
    }

    private void updateUserSubscription(User user, Subscription subscription) throws UserManagementException {
        getSecurityService().updateUserSubscription(user.getName(), subscription);
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

    private void sendSuccess(HttpServletResponse response) {
        response.setStatus(Response.Status.OK.getStatusCode());
    }

    private void sendFail(HttpServletResponse response) {
        response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}
