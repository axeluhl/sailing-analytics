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
    public String generateHostedPageObject() {
        try {
            User user = getCurrentUser();
            
            String[] userNameParts = user.getFullName().split("\\s+");
            String firstName = userNameParts[0];
            String lastName = "";
            if (userNameParts.length > 1) {
                lastName = String.join(" ", Arrays.copyOfRange(userNameParts, 1, userNameParts.length));
            }
            
            String locale = user.getLocaleOrDefault().getLanguage();
            
            Result result = HostedPage.checkoutNew()
                            .customerEmail(user.getEmail())
                            .customerFirstName(firstName)
                            .customerLastName(lastName)
                            .customerLocale(locale)
                            .subscriptionPlanId(SubscriptionPlans.PREMIUM.getId())
                            .billingAddressFirstName(firstName)
                            .billingAddressLastName(lastName)
                            .billingAddressCountry("US").request();
            return result.hostedPage().toJson();
        } catch (Exception e) {
            e.printStackTrace();
            
            logger.log(Level.WARNING, "Error in generating Chargebee hosted page data ", e);
            
            return null;
        }
        
    }
    
    @Override
    public SubscriptionDTO upgradePlanSuccess(String hostedPageId) {
        SubscriptionDTO subscriptionDto = new SubscriptionDTO();
        
        try {
            User user = getCurrentUser();
            
            Result result = HostedPage.acknowledge(hostedPageId).request();
            logger.log(Level.INFO, "ACKRESULT " + result.jsonResponse().toString());
            Content content = result.hostedPage().content();
            Subscription subscription = new Subscription();
            subscription.hostedPageId = hostedPageId;
            subscription.subscriptionId = content.subscription().id();
            subscription.customerId = content.customer().id();
            subscription.planId = content.subscription().planId();
            subscription.trialStart = content.subscription().trialStart().getTime();
            subscription.trialEnd = content.subscription().trialEnd().getTime();
            subscription.transactionStatus = content.subscription().status().name();
            subscription.subsciptionCreatedAt = content.subscription().createdAt().getTime();
            subscription.subsciptionUpdatedAt = content.subscription().updatedAt().getTime();
            
            getSecurityService().updateUserSubscription(user.getName(), subscription);
            
            subscriptionDto.planId = subscription.planId;
            subscriptionDto.trialStart = subscription.trialStart;
            subscriptionDto.trialEnd = subscription.trialEnd;
            subscriptionDto.transactionStatus = subscription.transactionStatus;
            
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
            if (subscription == null) {
                return null;
            }
            
            subscriptionDto.planId = subscription.planId;
            subscriptionDto.transactionStatus = subscription.transactionStatus;
            subscriptionDto.trialStart = subscription.trialStart;
            subscriptionDto.trialEnd = subscription.trialEnd;
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.WARNING, "Error in getting subscription ", e);
            
            subscriptionDto.error = e.getMessage();
        }
        
        return subscriptionDto;
    }
    
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
