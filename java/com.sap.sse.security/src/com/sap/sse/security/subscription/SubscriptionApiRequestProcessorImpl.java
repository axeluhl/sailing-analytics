package com.sap.sse.security.subscription;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

/**
 * Implementation of {@code SubscriptionApiRequest}. An {@link #executor} is used to schedule request execution. The
 * service's rate limit is respected by delaying subsequent requests appropriately. In case an API rate limit error occurs,
 * the request is expected to invoke {@link SubscriptionApiRequestProcessor#rescheduleRequestAfterRateLimitExceeded(SubscriptionApiRequest)}
 * to re-schedule with a larger delay.
 */
public class SubscriptionApiRequestProcessorImpl implements SubscriptionApiRequestProcessor {
    private static final Logger logger = Logger.getLogger(SubscriptionApiRequestProcessorImpl.class.getName());

    private final ScheduledExecutorService executor;

    /**
     * The latest time point for which a request was scheduled by this processor using the {@link #executor}, keyed by
     * the provider name (see {@link SubscriptionApiService#getProviderName()}). {@code null} means that no request has
     * been scheduled yet. Reading and updating this field needs to be thread safe and must be done while holding this
     * object's monitor ({@code synchronized}). See the {@link #getDelayToNextRequestStartTimePoint(String)} and
     * {@link #getDelayWhenRateLimitWasExceeded(String)} methods which implement this for regular requests and for requests
     * that have to be re-scheduled after the rate limit has been exceeded (despite our attempts done here to respect
     * the rate limits).
     */
    private ConcurrentHashMap<String, TimePoint> startOfLatestRequestByProviderName;

    public SubscriptionApiRequestProcessorImpl(ScheduledExecutorService executor) {
        this.executor = executor;
        this.startOfLatestRequestByProviderName = new ConcurrentHashMap<>();
    }

    @Override
    public void addRequest(SubscriptionApiRequest request) {
        scheduleRequest(request, getDelayToNextRequestStartTimePoint(request.getSubscriptionApiBaseService()));
    }

    @Override
    public void rescheduleRequestAfterRateLimitExceeded(SubscriptionApiRequest request) {
        scheduleRequest(request, getDelayWhenRateLimitWasExceeded(request.getSubscriptionApiBaseService()));
    }
    
    /**
     * Schedules the {@code request} with the {@link #executor} for execution after the {@code delay} has passed. If the
     * request fails with an exception, a {@link Level#SEVERE} log message is produced, but the request is not
     * re-scheduled. Note that the request itself may choose to handle exceptions such as exceeding the rate limit and
     * may choose to {@link #rescheduleRequestAfterRateLimitExceeded(SubscriptionApiRequest) reschedule itself}.
     */
    private void scheduleRequest(SubscriptionApiRequest request, Duration delay) {
        assert request != null;
        executor.schedule(() -> {
            try {
                request.run();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Process subscription API request error", e);
            }
        }, delay.asMillis(), TimeUnit.MILLISECONDS);
    }
    
    private Duration getDelayWhenRateLimitWasExceeded(SubscriptionApiBaseService subscriptionApiBaseService) {
        final TimePoint now = TimePoint.now();
        synchronized (this) {
            startOfLatestRequestByProviderName.put(subscriptionApiBaseService.getProviderName(),
                    now.plus(subscriptionApiBaseService.getLimitReachedResumeDelay()));
        }
        return subscriptionApiBaseService.getLimitReachedResumeDelay();
    }

    private Duration getDelayToNextRequestStartTimePoint(SubscriptionApiBaseService subscriptionApiBaseService) {
        final TimePoint now = TimePoint.now();
        TimePoint startOfLatestRequest;
        synchronized (this) {
            if (!startOfLatestRequestByProviderName.containsKey(subscriptionApiBaseService.getProviderName())) {
                startOfLatestRequest = now;
            } else {
                startOfLatestRequest = startOfLatestRequestByProviderName
                        .get(subscriptionApiBaseService.getProviderName()).plus(subscriptionApiBaseService.getTimeBetweenApiRequestStart());
                if (startOfLatestRequest.before(now)) {
                    startOfLatestRequest = now;
                }
            }
            startOfLatestRequestByProviderName.put(subscriptionApiBaseService.getProviderName(), startOfLatestRequest);
        }
        return now.until(startOfLatestRequest);
    }
}
