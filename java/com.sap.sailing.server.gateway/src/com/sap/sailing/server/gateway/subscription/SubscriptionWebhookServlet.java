package com.sap.sailing.server.gateway.subscription;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.sap.sailing.server.gateway.SailingServerHttpServlet;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sse.security.shared.Subscription;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.impl.User;

public class SubscriptionWebhookServlet extends SailingServerHttpServlet {
    private static final long serialVersionUID = 2608645647937414012L;
    private static final Logger logger = Logger.getLogger(SubscriptionWebhookServlet.class.getName());
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Object requestBody = JSONValue.parseWithException(request.getReader());
            JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);
            logger.log(Level.INFO, "Chargebee webhook data: " + requestObject.toJSONString());
            
            SubscriptionWebhookEvent event = new SubscriptionWebhookEvent(requestObject);
            if (!event.isValidEvent()) {
                throw new Exception("Invalid webhook event");
            }
            
            User user = getUser(event.getCustomerId());
            if (user == null) {
                response.setStatus(200);
                return;
            }
            
            Subscription userSubscription = user.getSubscription();
            
            if (userSubscription != null && event.getEventOccurredAt() < user.getSubscription().latestEventTime) {
                response.setStatus(200);
                return;
            }
            
            switch (event.getEventType()) {
            case SubscriptionWebhookEvent.EVENT_CUSTOMER_DELETED:
                updateUserSubscription(user, null);
                break;
            case SubscriptionWebhookEvent.EVENT_SUBSCRIPTION_CANCELLED:
            case SubscriptionWebhookEvent.EVENT_SUBSCRIPTION_DELETED:
                if (userSubscription != null && userSubscription.subscriptionId.equals(event.getSubscriptionId())) {
                    updateUserSubscription(user, null);
                }
                break;
            case SubscriptionWebhookEvent.EVENT_SUBSCRIPTION_CREATED:
            case SubscriptionWebhookEvent.EVENT_SUBSCRIPTION_CHANGED:
            case SubscriptionWebhookEvent.EVENT_SUBSCRIPTION_ACTIVATED:
            case SubscriptionWebhookEvent.EVENT_PAYMENT_SUCCEEDED:
            case SubscriptionWebhookEvent.EVENT_PAYMENT_FAILED:
                updateUserSubscription(user, buildSubscription(userSubscription, event));
                break;
            }
            
            response.setStatus(200);
        } catch (ParseException e) {
            e.printStackTrace();
            logger.log(Level.WARNING, "Failed to parse subscription webhook event data: " + e.getMessage());
            response.setStatus(500);
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.WARNING, "Failed to proccess subscription webhook event: " + e.getMessage());
            response.setStatus(500);
        }
    }
    
    private Subscription buildSubscription(Subscription currentSubscription, SubscriptionWebhookEvent event) throws CloneNotSupportedException {
        Subscription subscription = new Subscription();
        
        subscription.subscriptionId = event.getSubscriptionId();
        subscription.planId = event.getPlanId();
        subscription.customerId = event.getCustomerId();
        subscription.trialStart = event.getSubscriptionTrialStart();
        subscription.trialEnd = event.getSubscriptionTrialEnd();
        subscription.subscriptionStatus = event.getSubscriptionStatus();
        subscription.subsciptionCreatedAt = event.getSubscriptionCreatedAt();
        subscription.subsciptionUpdatedAt = event.getSubscriptionUpdatedAt();
        subscription.latestEventTime = event.getEventOccurredAt();
        
        
        if (subscription.subscriptionStatus != null && subscription.subscriptionStatus.equals(SubscriptionWebhookEvent.SUBSCRIPTION_STATUS_ACTIVE)) {
            String paymentStatus = getEventPaymentStatus(event);
            if (paymentStatus != null) {
                subscription.paymentStatus = paymentStatus;
            } else {
                subscription.paymentStatus = currentSubscription != null ? currentSubscription.paymentStatus : null;
            }
        } else {
            subscription.paymentStatus = null;
        }
        
        return subscription;
    }
    
    private User getUser(String customerId) {
        return getSecurityService().getUserByName(customerId);
    }
    
    private void updateUserSubscription(User user, Subscription subscription) throws UserManagementException {
        getSecurityService().updateUserSubscription(user.getName(), subscription);
    }
    
    private String getEventPaymentStatus(SubscriptionWebhookEvent event) {
        String paymentStatus = null;
        String transactionStatus = event.getTransactionStatus();
        if (transactionStatus == null) {
            String invoiceStatus = event.getInvoiceStatus();
            if (invoiceStatus != null) {
                paymentStatus = invoiceStatus.equals(SubscriptionWebhookEvent.INVOICE_STATUS_PAID) ?
                            Subscription.PAYMENT_STATUS_SUCCESS :
                            Subscription.PAYMENT_STATUS_NO_SUCCESS;
            }
        } else {
            String transactionType = event.getTransactionType();
            if (transactionType != null && transactionType.equals(SubscriptionWebhookEvent.TRANSACTION_TYPE_PAYMENT)) {
                paymentStatus = transactionStatus.equals(SubscriptionWebhookEvent.TRANSACTION_STATUS_SUCCESS) ? 
                                    Subscription.PAYMENT_STATUS_SUCCESS : 
                                    Subscription.PAYMENT_STATUS_NO_SUCCESS;
            }
        }
        
        return paymentStatus;
    }
}
