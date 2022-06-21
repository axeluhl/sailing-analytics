package com.sap.sse.security.subscription;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.security.SecurityService;
import com.sap.sse.util.ServiceTrackerFactory;
import com.sap.sse.util.ThreadPoolUtil;

/**
 * Manage to start a schedule task to perform fetching, checking and updating subscriptions for users from payment
 * service providers. Call {@code #start(CompletableFuture)} to schedule task in background thread.
 */
public class SubscriptionPlanBackgroundUpdater {
    private static final Logger logger = Logger.getLogger(SubscriptionPlanBackgroundUpdater.class.getName());

    private final ScheduledExecutorService executor;
    private final ServiceTracker<SubscriptionApiService, SubscriptionApiService> subscriptionApiServiceTracker;

    public SubscriptionPlanBackgroundUpdater(BundleContext context) {
        this.executor = ThreadPoolUtil.INSTANCE.getDefaultBackgroundTaskThreadPoolExecutor();
        subscriptionApiServiceTracker = ServiceTrackerFactory.createAndOpen(context, SubscriptionApiService.class);
    }

    public void start(CompletableFuture<SecurityService> securityService) {
        logger.info(() -> "Start subscription plan background update task");
        executor.schedule(new SubscriptionPlanUpdateTask(securityService, subscriptionApiServiceTracker),
                /* initial */ 1, TimeUnit.MINUTES);
    }
}
