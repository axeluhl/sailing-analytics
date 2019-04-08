package com.sap.sse.mail.queue.impl;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.mail.MailService;
import com.sap.sse.mail.queue.MailNotification;
import com.sap.sse.mail.queue.MailQueue;
import com.sap.sse.util.ThreadPoolUtil;

/**
 * {@link MailQueue} implementation that uses {@link ThreadPoolUtil#getDefaultBackgroundTaskThreadPoolExecutor()} to
 * process the {@link MailNotification}s.
 */
public class ExecutorMailQueue implements MailQueue {
    private final ServiceTracker<MailService, MailService> mailServiceTracker;

    public ExecutorMailQueue(ServiceTracker<MailService, MailService> mailServiceTracker) {
        this.mailServiceTracker = mailServiceTracker;
    }

    @Override
    public void stop() {
    }

    @Override
    public void addNotification(MailNotification notification) {
        ThreadPoolUtil.INSTANCE.getDefaultBackgroundTaskThreadPoolExecutor()
                .execute(() -> notification.sendNotifications(mailServiceTracker.getService()));
    }
}
