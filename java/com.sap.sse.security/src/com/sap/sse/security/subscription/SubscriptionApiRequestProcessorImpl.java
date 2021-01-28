package com.sap.sse.security.subscription;

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
    /**
     * Chargebee has API rate limits Threshold value for test site: ~750 API calls in 5 minutes. Threshold value for
     * live site: ~150 API calls per site per minute. So to prevent the limit would be reached, a request has a frame of
     * ~400ms, and a next request should be made after 400ms from previous request.
     */
    private static final Duration TIME_BETWEEN_API_REQUEST_START = Duration.ONE_MILLISECOND.times(400);

    /**
     * The delay after which to re-schedule a request that failed for having exceeded the service's rate limit
     */
    private static final Duration LIMIT_REACHED_RESUME_DELAY = Duration.ONE_MILLISECOND.times(65000);

    private final ScheduledExecutorService executor;

    private TimePoint startOfLatestRequest;

    public SubscriptionApiRequestProcessorImpl(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void addRequest(SubscriptionApiRequest request) {
        scheduleRequest(request, getDelayToNextRequestStartTimePoint());
    }

    @Override
    public void rescheduleRequestAfterRateLimitExceeded(SubscriptionApiRequest request) {
        scheduleRequest(request, getDelayWhenRateLimitWasExceeded());
    }
    
    /**
     * @param entry can be {@code null} which makes this call a no-op
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
    
    private Duration getDelayWhenRateLimitWasExceeded() {
        final TimePoint now = TimePoint.now();
        synchronized (this) {
            startOfLatestRequest = now.plus(LIMIT_REACHED_RESUME_DELAY);
        }
        return LIMIT_REACHED_RESUME_DELAY;
    }

    private Duration getDelayToNextRequestStartTimePoint() {
        final TimePoint now = TimePoint.now();
        synchronized (this) {
            if (startOfLatestRequest == null) {
                startOfLatestRequest = now;
            } else {
                startOfLatestRequest = startOfLatestRequest.plus(TIME_BETWEEN_API_REQUEST_START);
                if (startOfLatestRequest.before(now)) {
                    startOfLatestRequest = now;
                }
            }
        }
        return now.until(startOfLatestRequest);
    }
}
