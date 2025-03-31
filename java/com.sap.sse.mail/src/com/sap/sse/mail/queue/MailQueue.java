package com.sap.sse.mail.queue;

import com.sap.sse.common.Stoppable;
import com.sap.sse.mail.MailService;

/**
 * Supports sending mails via {@link MailService} in a deferred way by enqueuing {@link MailNotification} instances.
 */
public interface MailQueue extends Stoppable {
    void addNotification(MailNotification notification);
}
