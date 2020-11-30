package com.sap.sse.security.subscription;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.common.TimePoint;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.SubscriptionData;

/**
 * Task to perform fetching, checking and updating user subscriptions from payment service providers. See
 * {@code SubscriptionBackgroundUpdate#start(CompletableFuture)}
 */
public class SubscriptionUpdateTask implements Runnable {
    private static final Logger logger = Logger.getLogger(SubscriptionUpdateTask.class.getName());

    private final CompletableFuture<SecurityService> securityService;

    private final ServiceTracker<SubscriptionApiService, SubscriptionApiService> subscriptionApiServiceTracker;

    public SubscriptionUpdateTask(CompletableFuture<SecurityService> securityService, ServiceTracker<SubscriptionApiService, SubscriptionApiService> subscriptionApiServiceTracker) {
        this.securityService = securityService;
        this.subscriptionApiServiceTracker = subscriptionApiServiceTracker;
    }

    @Override
    public void run() {
        Iterable<User> users = getSecurityService().getUserList();
        for (User user : users) {
            fetchAndUpdateUserSubscriptions(user);
        }
    }

    private void fetchAndUpdateUserSubscriptions(User user) {
        logger.info(() -> "Start checking and updating subscriptions for user: " + user.getName());
        for (final ServiceReference<SubscriptionApiService> serviceReference : subscriptionApiServiceTracker.getServiceReferences()) {
            final SubscriptionApiService apiService = subscriptionApiServiceTracker.getService(serviceReference);
            if (apiService != null) {
                try {
                    Iterable<Subscription> userSubscriptions = apiService.getUserSubscriptions(user);
                    checkAndUpdateSubscriptions(user, userSubscriptions, apiService);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to fetch and update subscriptions for user " + user.getName(), e);
                }
            }
        }
    }

    private void checkAndUpdateSubscriptions(User user, Iterable<Subscription> userSubscriptions,
            SubscriptionApiService provider) throws UserManagementException {
        logger.info(() -> "Subscriptions from provider " + provider.getProviderName() + " for user " + user.getName()
                + ": " + userSubscriptions);
        Iterable<Subscription> currentSubscriptions = user.getSubscriptions();
        if ((userSubscriptions == null || !userSubscriptions.iterator().hasNext())
                && (currentSubscriptions != null && currentSubscriptions.iterator().hasNext())) {
            // No subscriptions so we need to remove all current subscriptions of user for the provider
            Subscription emptySubscription = createEmptySubscription(provider, null);
            getSecurityService().updateUserSubscription(user.getName(), emptySubscription);
            // getSecurityService().removeProviderUserSubscriptions(user.getName(), providerName);
        } else if (userSubscriptions != null) {
            if (currentSubscriptions != null) {
                Map<String, Boolean> existingPlans = getExistingPlans(userSubscriptions);
                for (Subscription subscription : currentSubscriptions) {
                    if (subscription.hasPlan() && !existingPlans.containsKey(subscription.getPlanId())) {
                        // Current subscription plan doesn't exist in subscription list from provider, that means
                        // subscription for the plan has been deleted, then we need to remove the subscription from
                        // database
                        Subscription emptySubscription = createEmptySubscription(provider, subscription.getPlanId());
                        getSecurityService().updateUserSubscription(user.getName(), emptySubscription);
                    }
                }
            }

            for (Subscription subscription : userSubscriptions) {
                getSecurityService().updateUserSubscription(user.getName(), subscription);
            }
        }
    }

    private Subscription createEmptySubscription(SubscriptionApiService provider, String planId) {
        return provider.getDataHandler().toSubscription(
                SubscriptionData.createEmptySubscriptionDataWithUpdateTimes(planId, TimePoint.now(), TimePoint.now()));
    }

    private Map<String, Boolean> getExistingPlans(Iterable<Subscription> subscriptions) {
        final Map<String, Boolean> existingPlans = new HashMap<String, Boolean>();
        for (Subscription subscription : subscriptions) {
            if (subscription.hasPlan()) {
                existingPlans.put(subscription.getPlanId(), true);
            }
        }
        return existingPlans;
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
