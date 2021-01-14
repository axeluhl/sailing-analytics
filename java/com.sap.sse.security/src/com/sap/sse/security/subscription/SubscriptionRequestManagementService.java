package com.sap.sse.security.subscription;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service to schedule APIs requests to subscription provider in background thread
 */
public class SubscriptionRequestManagementService {
    private final ScheduledExecutorService executor;

    public SubscriptionRequestManagementService(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Schedule a request, and handle resume in case the request needed to be resumed
     */
    public void scheduleRequest(SubscriptionApiRequest request, long delayMs, long resumeDelayMs) {
//        request.setRequestManagementService(this);
//        executor.schedule(() -> {
//            request.run();
//            if (request.needResume()) {
//                scheduleRequest(request, resumeDelayMs, resumeDelayMs);
//            }
//        }, delayMs, TimeUnit.MILLISECONDS);
    }
}
