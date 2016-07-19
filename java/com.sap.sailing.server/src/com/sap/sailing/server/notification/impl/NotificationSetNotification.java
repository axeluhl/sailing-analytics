package com.sap.sailing.server.notification.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.mail.MailService;
import com.sap.sse.mail.queue.MailNotification;
import com.sap.sse.security.PreferenceObjectBasedNotificationSet;

public abstract class NotificationSetNotification<T> implements MailNotification {
    private static final Logger logger = Logger.getLogger(NotificationSetNotification.class.getName());
    
    private static final String TEMPLATE_FILE = "notification-mail-template.html";
    private static final String TEMPLATE = loadTemplateFile();

    private static String loadTemplateFile() {
        StringBuilder content = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                NotificationSetNotification.class.getResourceAsStream(TEMPLATE_FILE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException exc) {
            logger.log(Level.SEVERE, "Error while loading notification mail template!", exc);
        }
        return content.toString();
    }


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
                NotificationMailTemplate mailTemplate = getMailTemplate(objectToNotifyAbout, locale);
                bodyPart.setContent(getMailContent(mailTemplate), "text/html");
                multipart.addBodyPart(bodyPart);
                mailService.sendMail(user.getEmail(), mailTemplate.getSubject(), multipart);
            } catch (Exception e) {
                logger.log(Level.SEVERE,
                        "Could not send mail notification for \"" + objectToNotifyAbout + "\" to user \" + user + \"", e);
            }
        });
    }
    
    private String getMailContent(NotificationMailTemplate notificationMailTemplate) {
        StringBuilder bodyContent = new StringBuilder();
        if (notificationMailTemplate.getTitle() != null) {
            bodyContent.append("<h1>").append(notificationMailTemplate.getTitle()).append("</h1>");
        }
        bodyContent.append("<p class=\"textContainer\">").append(notificationMailTemplate.getText()).append("</p>");
        for (Pair<String, String> link : notificationMailTemplate.getLabelsAndLinkUrls()) {
            bodyContent.append("<div class=\"buttonContainer\">").append("<a class=\"linkButton\" href=\"");
            bodyContent.append(link.getB()).append("\">").append(link.getA()).append("</a>").append("</div>");
        }
        return TEMPLATE.replace("${title}", notificationMailTemplate.getSubject()).replace("${body}", bodyContent.toString());
    }
    
    protected abstract NotificationMailTemplate getMailTemplate(T objectToNotifyAbout, Locale locale);

}