package com.sap.sse.security.subscription;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of {@code SubscriptionApiRequest}. Requests will be stored in queue, and executor will process next
 * request only if current request is finished. In case API rate limit error occurs, the request should be re-added to
 * the queue and will be processed later.
 */
public class SubscriptionApiRequestProcessorImpl implements SubscriptionApiRequestProcessor {
    private static final Logger logger = Logger.getLogger(SubscriptionApiRequestProcessorImpl.class.getName());

    private final ScheduledExecutorService executor;

    private final RequestQueue requestQueue;

    public SubscriptionApiRequestProcessorImpl(ScheduledExecutorService executor) {
        this.executor = executor;
        this.requestQueue = new RequestQueue();
    }

    @Override
    public void process() {
        processNextRequestIfAvailable();
    }

    @Override
    public void addRequest(SubscriptionApiRequest request, long delayMs) {
        if (request != null) {
            RequestQueueEntry entry = requestQueue
                    .addAndGetNextRequestIfNotProcessing(new RequestQueueEntry(request, delayMs));
            scheduleRequest(entry);
        }
    }

    private void processNextRequestIfAvailable() {
        RequestQueueEntry entry = requestQueue.getNextRequestIfAvailable();
        scheduleRequest(entry);
    }

    private void scheduleRequest(RequestQueueEntry entry) {
        if (entry != null) {
            executor.schedule(() -> {
                try {
                    entry.getRequest().run();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Process subscription API request error", e);
                } finally {
                    processNextRequestIfAvailable();
                }
            }, entry.getDelayMs(), TimeUnit.MILLISECONDS);
        }
    }

    private static class RequestQueue {
        private final Queue<RequestQueueEntry> requestQueue;
        private boolean entryProcessing;

        public RequestQueue() {
            requestQueue = new LinkedList<RequestQueueEntry>();
            entryProcessing = false;
        }

        public synchronized RequestQueueEntry getNextRequestIfAvailable() {
            final RequestQueueEntry entry;
            if (!requestQueue.isEmpty()) {
                entryProcessing = true;
                entry = requestQueue.poll();
            } else {
                entryProcessing = false;
                entry = null;
            }
            return entry;
        }

        public synchronized RequestQueueEntry addAndGetNextRequestIfNotProcessing(RequestQueueEntry request) {
            requestQueue.add(request);
            final RequestQueueEntry entry;
            if (!entryProcessing) {
                entry = getNextRequestIfAvailable();
            } else {
                entry = null;
            }
            return entry;
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
