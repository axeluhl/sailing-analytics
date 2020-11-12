package com.sap.sse.security.subscription;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.SubscriptionApiService;
import com.sap.sse.security.shared.subscription.SubscriptionFactory;
import com.sap.sse.security.shared.subscription.SubscriptionProvider;

/**
 * Task to perform fetching, checking and updating user subscriptions from payment service providers. See
 * {@code SubscriptionBackgroundUpdate#start(CompletableFuture)}
 */
public class SubscriptionUpdateTask implements Runnable {
    private static final Logger logger = Logger.getLogger(SubscriptionUpdateTask.class.getName());

    private CompletableFuture<SecurityService> securityService;

    public SubscriptionUpdateTask(CompletableFuture<SecurityService> securityService) {
        this.securityService = securityService;
    }

    @Override
    public void run() {
        Iterable<User> users = getSecurityService().getUserList();
        Iterable<SubscriptionProvider> subscriptionProviders = SubscriptionFactory.getInstance().getProviders();
        for (User user : users) {
            fetchAndUserSubscriptions(user, subscriptionProviders);
        }
    }

    private void fetchAndUserSubscriptions(User user, Iterable<SubscriptionProvider> subscriptionProviders) {
        for (SubscriptionProvider provider : subscriptionProviders) {
            SubscriptionApiService apiService = provider.getApiService();
            if (apiService != null) {
                try {
                    Iterable<Subscription> userSubscriptions = apiService.getUserSubscriptions(user);
                    checkAndUpdateSubscriptions(user, userSubscriptions);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to fetch and update subscriptions for user " + user.getName(), e);
                }
            }
        }
    }

    private void checkAndUpdateSubscriptions(User user, Iterable<Subscription> userSubscriptions) {
        // TODO: implement checking and updating subscriptions
    }

    private SecurityService getSecurityService() {
        try {
            return securityService.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Failure to get SecurityService", e);
            throw new RuntimeException(e);
        }
    }
}
