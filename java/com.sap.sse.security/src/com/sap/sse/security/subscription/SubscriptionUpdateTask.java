package com.sap.sse.security.subscription;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.security.SecurityService;

/**
 * Task to perform fetching, checking and updating user subscriptions from payment service providers registered with the
 * OSGi service registry under the {@link SubscriptionApiService} interface. See
 * {@code SubscriptionBackgroundUpdater#start(CompletableFuture)}.
 */
public class SubscriptionUpdateTask implements Runnable {
    private static final Logger logger = Logger.getLogger(SubscriptionUpdateTask.class.getName());

    private final CompletableFuture<SecurityService> securityService;

    private final ServiceTracker<SubscriptionApiService, SubscriptionApiService> subscriptionApiServiceTracker;

    public SubscriptionUpdateTask(CompletableFuture<SecurityService> securityService,
            ServiceTracker<SubscriptionApiService, SubscriptionApiService> subscriptionApiServiceTracker) {
        this.securityService = securityService;
        this.subscriptionApiServiceTracker = subscriptionApiServiceTracker;
    }

    @Override
    public void run() {
        try {
            if (securityService.get().getMasterDescriptor() == null) {
                for (final ServiceReference<SubscriptionApiService> serviceReference : subscriptionApiServiceTracker
                        .getServiceReferences()) {
                    final SubscriptionApiService apiService = subscriptionApiServiceTracker.getService(serviceReference);
                    if (apiService != null && apiService.isActive()) {
                        logger.info("Fetching and updating provider subscriptions for API service "+apiService.getProviderName());
                        fetchAndUpdateProviderSubscriptions(apiService);
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.warning("Couldn't get a hold of the security service ("+e.getMessage()+"); not updating subscriptions.");
        }
    }

    private void fetchAndUpdateProviderSubscriptions(SubscriptionApiService apiService) {
        new ProviderSubscriptionUpdateTask(apiService, getSecurityService().getUserList(), securityService).run();
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
