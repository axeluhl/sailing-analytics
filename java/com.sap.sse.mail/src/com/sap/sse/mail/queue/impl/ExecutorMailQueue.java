package com.sap.sse.mail.queue.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.mail.MailService;
import com.sap.sse.mail.queue.MailNotification;
import com.sap.sse.mail.queue.MailQueue;

/**
 * {@link MailQueue} implementation that uses a single threaded executor to process the {@link MailNotification}s.
 */
public class ExecutorMailQueue implements MailQueue {
    private final ServiceTracker<MailService, MailService> mailServiceTracker;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(
            (runnable) -> {
                Thread thread = new Thread(runnable, ExecutorMailQueue.class.getName() + " executor");
                thread.setDaemon(true);
                return thread;
            });

    public ExecutorMailQueue(ServiceTracker<MailService, MailService> mailServiceTracker) {
        this.mailServiceTracker = mailServiceTracker;
    }

    @Override
    public void stop() {
        executor.shutdownNow();
    }

    @Override
    public void addNotification(MailNotification notification) {
        executor.execute(() -> notification.sendNotifications(mailServiceTracker.getService()));
    }
}
