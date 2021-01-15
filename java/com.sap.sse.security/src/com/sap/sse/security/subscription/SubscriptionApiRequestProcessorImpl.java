package com.sap.sse.security.subscription;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of {@code SubscriptionApiRequest}. Requests will be stored in queue, and executor will process next
 * request only if current request is finished. In case API rate limit error occurs, the request should be re-added to
 * the queue and will be processed later.
 */
public class SubscriptionApiRequestProcessorImpl implements SubscriptionApiRequestProcessor {
    private static final Logger logger = Logger.getLogger(SubscriptionApiRequestProcessorImpl.class.getName());

    private final Queue<RequestQueueEntry> requestQueue;
    private final ScheduledExecutorService executor;

    private AtomicBoolean processing = new AtomicBoolean(false);

    public SubscriptionApiRequestProcessorImpl(ScheduledExecutorService executor) {
        requestQueue = new ConcurrentLinkedQueue<RequestQueueEntry>();
        this.executor = executor;
    }

    @Override
    public void process() {
        processNextRequest();
    }

    @Override
    public void addRequest(SubscriptionApiRequest request, long delayMs) {
        if (request != null) {
            requestQueue.add(new RequestQueueEntry(request, delayMs));
            if (!processing.get()) {
                processNextRequest();
            }
        }
    }

    private void processNextRequest() {
        if (!requestQueue.isEmpty()) {
            processing.set(true);
            RequestQueueEntry entry = requestQueue.poll();
            executor.schedule(() -> {
                try {
                    entry.getRequest().run();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Process subscription API request error", e);
                } finally {
                    if (requestQueue.isEmpty()) {
                        processing.set(false);
                    } else {
                        processNextRequest();
                    }
                }
            }, entry.getDelayMs(), TimeUnit.MILLISECONDS);
        } else {
            processing.set(false);
        }
    }

    private static class RequestQueueEntry {
        private final SubscriptionApiRequest request;
        private final long delayMs;

        public RequestQueueEntry(SubscriptionApiRequest request, long delayMs) {
            this.request = request;
            this.delayMs = delayMs;
        }

        public SubscriptionApiRequest getRequest() {
            return request;
        }

        public long getDelayMs() {
            return delayMs;
        }
    }

}
