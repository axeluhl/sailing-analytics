package com.sap.sailing.gwt.ui.server.subscription;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.shiro.SecurityUtils;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.chargebee.Environment;
import com.chargebee.Result;
import com.chargebee.models.HostedPage;
import com.chargebee.models.HostedPage.Content;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.gwt.ui.client.subscription.SubscriptionService;
import com.sap.sailing.gwt.ui.shared.subscription.HostedPageResultDTO;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionPlans;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.Subscription;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.ui.server.Activator;

public class SubscriptionServiceImpl extends RemoteServiceServlet implements SubscriptionService {
    private static final long serialVersionUID = -4276839013785711262L;

    private static final Logger logger = Logger.getLogger(SubscriptionServiceImpl.class.getName());
    
    private final BundleContext context;
    private final FutureTask<SecurityService> securityService;
    
    public SubscriptionServiceImpl() {
        Environment.configure(
                SubscriptionConfiguration.getInstance().getSite(),
                SubscriptionConfiguration.getInstance().getApiKey());
        
        context = Activator.getContext();
        final ServiceTracker<SecurityService, SecurityService> tracker = new ServiceTracker<>(context, SecurityService.class, /* customizer */ null);
        tracker.open();
        securityService = new FutureTask<SecurityService>(new Callable<SecurityService>() {
            @Override
            public SecurityService call() {
                SecurityService result = null;
                try {
                    logger.info("Waiting for SecurityService...");
                    result = tracker.waitForService(0);
                    logger.info("Obtained SecurityService "+result);
                    return result;
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Interrupted while waiting for UserStore service", e);
                }
                return result;
            }
        });
        new Thread("ServiceTracker in bundle com.sap.sailing.gwt.ui.server for SecurityService") {
            @Override
            public void run() {
                securityService.run();
                SecurityUtils.setSecurityManager(getSecurityService().getSecurityManager());
            }
        }.start();
    }

    @Override
    public HostedPageResultDTO generateHostedPageObject(String planId) {
        HostedPageResultDTO response = new HostedPageResultDTO();
        if (planId == null || planId.isEmpty() || SubscriptionPlans.getPlan(planId) == null) {
            response.error = "Invalid plan";
            return response;
        }
        
        try {
            User user = getCurrentUser();
            
            if (user.getSubscription() != null && user.getSubscription().planId != null && user.getSubscription().planId.equals(planId)) {
                response.error = "User has already subscribed to " + SubscriptionPlans.getPlan(planId).getName() + " plan";
                return response;
            }
            
            Result result;
            
            if (user.getSubscription() == null || user.getSubscription().planId == null) {
                String[] userNameParts = user.getFullName().split("\\s+");
                String firstName = userNameParts[0];
                String lastName = "";
                if (userNameParts.length > 1) {
                    lastName = String.join(" ", Arrays.copyOfRange(userNameParts, 1, userNameParts.length));
                }
                
                String locale = user.getLocaleOrDefault().getLanguage();
                
                result = HostedPage.checkoutNew()
                        .customerId(user.getName())
                        .customerEmail(user.getEmail())
                        .customerFirstName(firstName)
                        .customerLastName(lastName)
                        .customerLocale(locale)
                        .subscriptionPlanId(planId)
                        .billingAddressFirstName(firstName)
                        .billingAddressLastName(lastName)
                        .billingAddressCountry("US").request();
            } else {
                result = HostedPage.checkoutExisting()
                        .subscriptionId(user.getSubscription().subscriptionId)
                        .subscriptionPlanId(planId)
                        .request();
            }
            
            response.hostedPageJSONString = result.hostedPage().toJson();
        } catch (Exception e) {
            e.printStackTrace();
            
            logger.log(Level.WARNING, "Error in generating Chargebee hosted page data ", e);
            
            response.error = "Error in generating Chargebee hosted page";
        }
        
        return response;
    }
    
    @Override
    public SubscriptionDTO updatePlanSuccess(String hostedPageId) {
        SubscriptionDTO subscriptionDto = new SubscriptionDTO();
        
        try {
            User user = getCurrentUser();
            
            Result result = HostedPage.acknowledge(hostedPageId).request();
            Content content = result.hostedPage().content();
            Subscription subscription = new Subscription();
            subscription.subscriptionId = content.subscription().id();
            subscription.customerId = content.customer().id();
            subscription.planId = content.subscription().planId();
            subscription.trialStart = Math.round(content.subscription().trialStart().getTime() / 1000);
            subscription.trialEnd = Math.round(content.subscription().trialEnd().getTime() / 1000);
            subscription.subscriptionStatus = content.subscription().status().name().toLowerCase();
            subscription.subsciptionCreatedAt = Math.round(content.subscription().createdAt().getTime() / 1000);
            subscription.subsciptionUpdatedAt = Math.round(content.subscription().updatedAt().getTime() / 1000);
            subscription.latestEventTime = 0;
            subscription.manualUpdatedAt = Math.round(System.currentTimeMillis() / 1000);
            
            getSecurityService().updateUserSubscription(user.getName(), subscription);
            
            subscriptionDto.planId = subscription.planId;
            subscriptionDto.trialStart = subscription.trialStart;
            subscriptionDto.trialEnd = subscription.trialEnd;
            subscriptionDto.subscriptionStatus = subscription.subscriptionStatus;
            subscriptionDto.paymentStatus = subscription.paymentStatus;
            
            return subscriptionDto;
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.WARNING, "Error in saving subscription ", e);
            
            subscriptionDto.error = e.getMessage();
            return subscriptionDto;
        }
    }
    
    @Override
    public SubscriptionDTO getSubscription() {
        SubscriptionDTO subscriptionDto = new SubscriptionDTO();
        try {
            User user = getCurrentUser();
            
            Subscription subscription = user.getSubscription();
            if (subscription == null || subscription.planId == null || subscription.planId.isEmpty()) {
                return null;
            }
            
            subscriptionDto.planId = subscription.planId;
            subscriptionDto.subscriptionStatus = subscription.subscriptionStatus;
            subscriptionDto.paymentStatus = subscription.paymentStatus;
            subscriptionDto.trialStart = subscription.trialStart;
            subscriptionDto.trialEnd = subscription.trialEnd;
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.WARNING, "Error in getting subscription ", e);
            
            subscriptionDto.error = e.getMessage();
        }
        
        return subscriptionDto;
    }
    
    @Override
    public boolean cancelSubscription() {
        try {
            User user = getCurrentUser();
            Subscription subscription = user.getSubscription();
            if (subscription == null) {
                return true;
            }
            
            String subscriptionId = subscription.subscriptionId;
            if (subscriptionId != null && !subscriptionId.isEmpty()) {
                Result result = com.chargebee.models.Subscription.cancel(subscriptionId)
                                .request();
                if (!result.subscription().status().name().toLowerCase().equals(Subscription.SUBSCRIPTION_STATUS_CANCELLED)) {
                    return false;
                }
            }
            
            Subscription newSubscription = new Subscription();
            newSubscription.latestEventTime = subscription.latestEventTime;
            newSubscription.manualUpdatedAt = Math.round(System.currentTimeMillis() / 1000);
            getSecurityService().updateUserSubscription(user.getName(), newSubscription);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.WARNING, "Error in cancel subscription ", e);
            return false;
        }
    }

//    @Override
//    public String generatePortalPageObject() {
//        try {
//            User user = getCurrentUser();
//            Result result = PortalSession.create().customerId(user.getName()).request();
//            PortalSession portalSession = result.portalSession();
//            return portalSession.toJson();
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.log(Level.WARNING, "Error in generating portal session page object ", e);
//            return null;
//        }
//    }
    
    private SecurityService getSecurityService() {
        try {
            return securityService.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    
    private User getCurrentUser() throws SubscriptionException {
        User user = getSecurityService().getCurrentUser();
        if (user == null) {
            throw new SubscriptionException(SubscriptionException.INVALID_CURRENT_USER);
        }
        
        return user;
    }
    
    private class SubscriptionException extends Exception implements Serializable {
        private static final long serialVersionUID = 6321960099419330110L;
        
        public static final String INVALID_CURRENT_USER = "Current user not found";
        
        private final String message;
        
        @Override
        public String getMessage() {
            return message;
        }

        public SubscriptionException(String message) {
            this.message = message;
        }
    }
}
