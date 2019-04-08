package com.sap.sse.mail.queue;

import com.sap.sse.mail.MailService;

public interface MailNotification {
    void sendNotifications(final MailService mailService);
}