package com.sap.sailing.server.notification.impl;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import com.sap.sse.mail.MailService;
import com.sap.sse.mail.queue.MailNotification;
import com.sap.sse.security.PreferenceObjectBasedNotificationSet;

public abstract class NotificationSetNotification<T> implements MailNotification {
    private static final Logger logger = Logger.getLogger(NotificationSetNotification.class.getName());

    private final T objectToNotifyAbout;
    private final PreferenceObjectBasedNotificationSet<?, T> associatedNotificationSet;

    public NotificationSetNotification(T objectToNotifyAbout, PreferenceObjectBasedNotificationSet<?, T> associatedNotificationSet) {
        this.objectToNotifyAbout = objectToNotifyAbout;
        this.associatedNotificationSet = associatedNotificationSet;
    }

    @Override
    public void sendNotifications(final MailService mailService) {
        // TODO idea (c) by Axel Uhl: In case of performance/mail queue problems: group mail addresses by locale and
        // send batches with the actual mail addresses as bcc?
        associatedNotificationSet.forUsersWithVerifiedEmailMappedTo(objectToNotifyAbout, (user) -> {
            Locale locale = user.getLocaleOrDefault();
            Multipart multipart = new MimeMultipart();
            BodyPart bodyPart = new MimeBodyPart();
            try {
                bodyPart.setContent(constructBody(objectToNotifyAbout, locale), "text/html");
                multipart.addBodyPart(bodyPart);
                mailService.sendMail(user.getEmail(), constructSubject(objectToNotifyAbout, locale), multipart);
            } catch (Exception e) {
                logger.log(Level.SEVERE,
                        "Could not send mail notification for \"" + objectToNotifyAbout + "\" to user \" + user + \"", e);
            }
        });
    }

    protected abstract String constructSubject(T objectToNotifyAbout, Locale locale);

    protected abstract String constructBody(T objectToNotifyAbout, Locale locale);
}