package com.sap.sse.security.subscription;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SubscriptionApiRequestProcessorImpl implements SubscriptionApiRequestProcessor {
    private static final Logger logger = Logger.getLogger(SubscriptionApiRequestProcessorImpl.class.getName());

    private final Queue<RequestQueueEntry> requestQueue;
    private final ScheduledExecutorService executor;

    private boolean processing = false;

    public SubscriptionApiRequestProcessorImpl(ScheduledExecutorService executor) {
        requestQueue = new LinkedList<RequestQueueEntry>();
        this.executor = executor;
    }

    @Override
    public void process() {
        RequestQueueEntry entry = requestQueue.poll();
        if (entry != null && entry.getRequest() != null) {
            processing = true;
            executor.schedule(() -> {
                try {
                    entry.getRequest().run();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Process subscription API request error", e);
                } finally {
                    processing = false;
                    process();
                }
            }, entry.getDelayMs(), TimeUnit.MILLISECONDS);
        } else {
            processing = false;
        }
    }

    @Override
    public void addRequest(SubscriptionApiRequest request, long delayMs) {
        if (request != null) {
            requestQueue.add(new RequestQueueEntry(request, delayMs));
            if (!processing) {
                process();
            }
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
