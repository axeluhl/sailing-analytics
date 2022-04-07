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
 * Manage to start a schedule task to perfom fetching, checking and updating subscriptions for users from payment
 * service providers. Call {@code #start(CompletableFuture)} to schedule task in background thread.
 */
public class SubscriptionBackgroundUpdater {
    private static final Logger logger = Logger.getLogger(SubscriptionBackgroundUpdater.class.getName());

    private final ScheduledExecutorService executor;
    private final ServiceTracker<SubscriptionApiService, SubscriptionApiService> subscriptionApiServiceTracker;

    private boolean started;

    public SubscriptionBackgroundUpdater(BundleContext context) {
        this.executor = ThreadPoolUtil.INSTANCE.getDefaultBackgroundTaskThreadPoolExecutor();
        subscriptionApiServiceTracker = ServiceTrackerFactory.createAndOpen(context, SubscriptionApiService.class);
    }

    public void start(CompletableFuture<SecurityService> securityService) {
        if (!started) {
            logger.info(() -> "Start subscription background update task");
            executor.scheduleAtFixedRate(new SubscriptionUpdateTask(securityService, subscriptionApiServiceTracker),
                    /* initial */ 1, /* period */ 720, TimeUnit.MINUTES);
        }
    }
}
