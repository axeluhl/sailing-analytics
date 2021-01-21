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

    private final Queue<RequestQueueEntry> requestQueue;
    private final ScheduledExecutorService executor;

    private boolean processing = false;

    public SubscriptionApiRequestProcessorImpl(ScheduledExecutorService executor) {
        requestQueue = new LinkedList<RequestQueueEntry>();
        this.executor = executor;
    }

    @Override
    public void process() {
        processNextRequestIfAvailable();
    }

    @Override
    public synchronized void addRequest(SubscriptionApiRequest request, long delayMs) {
        if (request != null) {
            boolean isProcessing = addNewRequestAndDetermineIsProcessing(new RequestQueueEntry(request, delayMs));
            if (!isProcessing) {
                processNextRequestIfAvailable();
            }
        }
    }

    private void processNextRequestIfAvailable() {
        RequestQueueEntry entry = getNextRequestIfAvailable();
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

    private RequestQueueEntry getNextRequestIfAvailable() {
        final RequestQueueEntry entry;

        synchronized (requestQueue) {
            if (!requestQueue.isEmpty()) {
                processing = true;
                entry = requestQueue.poll();
            } else {
                processing = false;
                entry = null;
            }
            return entry;
        }
    }

    private boolean addNewRequestAndDetermineIsProcessing(RequestQueueEntry entry) {
        synchronized (requestQueue) {
            requestQueue.add(entry);
            return processing;
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
