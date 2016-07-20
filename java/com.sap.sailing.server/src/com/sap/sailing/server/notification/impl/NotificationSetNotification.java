package com.sap.sailing.server.notification.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.i18n.impl.ResourceBundleStringMessagesImpl;
import com.sap.sse.mail.MailService;
import com.sap.sse.mail.queue.MailNotification;
import com.sap.sse.security.PreferenceObjectBasedNotificationSet;
import com.sap.sse.security.User;

public abstract class NotificationSetNotification<T> implements MailNotification {
    private static final Logger logger = Logger.getLogger(NotificationSetNotification.class.getName());
    
    private static final String TEMPLATE_FILE = "notification-mail-template.html";
    private static final String LOGO_FILE = "sap_logo_header.png";

    private static final String TEMPLATE = loadTemplateFile();
    private static final byte[] LOGO_BYTES = loadLogoFile();

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

    private static byte[] loadLogoFile() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InputStream in = NotificationSetNotification.class.getResourceAsStream(LOGO_FILE)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) >= 0) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        } catch (IOException exc) {
            logger.log(Level.SEVERE, "Error while loading notification mail template!", exc);
            return null;
        }
    }

    private final T objectToNotifyAbout;
    private final PreferenceObjectBasedNotificationSet<?, T> associatedNotificationSet;
    private static final ResourceBundleStringMessagesImpl messages = new ResourceBundleStringMessagesImpl(
            SailingNotificationServiceImpl.STRING_MESSAGES_BASE_NAME,
            NotificationSetNotification.class.getClassLoader());

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
            BodyPart messageImagePart = new MimeBodyPart();
            try {
                // TODO: add cid:saplogo
                NotificationMailTemplate mailTemplate = getMailTemplate(objectToNotifyAbout, locale);
                bodyPart.setContent(getMailContent(mailTemplate, user, locale), "text/html");
                multipart.addBodyPart(bodyPart);

                DataSource imageDs = new ByteArrayDataSource(LOGO_BYTES, "image/png");
                messageImagePart.setDataHandler(new DataHandler(imageDs));
                messageImagePart.setHeader("Content-ID", "saplogo");
                messageImagePart.setHeader("Content-Disposition", "inline;filename=\"saplogo.png\"");
                multipart.addBodyPart(messageImagePart);

                mailService.sendMail(user.getEmail(), mailTemplate.getSubject(), multipart);
            } catch (Exception e) {
                logger.log(Level.SEVERE,
                        "Could not send mail notification for \"" + objectToNotifyAbout + "\" to user \" + user + \"", e);
            }
        });
    }
    
    private String getMailContent(NotificationMailTemplate notificationMailTemplate, User user, Locale locale) {
        StringBuilder bodyContent = new StringBuilder();
        if (notificationMailTemplate.getTitle() != null) {
            bodyContent.append("<h1>").append(notificationMailTemplate.getTitle()).append("</h1>");
        }
        String name = user.getFullName() == null || user.getFullName().isEmpty() ? user.getName() : user.getFullName();
        bodyContent.append("<p>").append(messages.get(locale, "salutation", name)).append("</p>");
        bodyContent.append("<p>").append(notificationMailTemplate.getText()).append("</p>");
        for (Pair<String, String> link : notificationMailTemplate.getLabelsAndLinkUrls()) {
            bodyContent.append("<div class=\"buttonContainer\">").append("<a class=\"linkButton\" href=\"");
            bodyContent.append(link.getB()).append("\">").append(link.getA()).append("</a>").append("</div>");
        }
        String siteLink = "<a href=\"" + notificationMailTemplate.getServerBaseUrl() + "/gwt/Home.html\">"
                + notificationMailTemplate.getServerBaseUrl() + "</a>";
        StringBuilder footerLinks = new StringBuilder();
        footerLinks //
                .append("<a href=\"") //
                .append(notificationMailTemplate.getServerBaseUrl())
                .append("/gwt/Home.html#/user/profile/:\">").append(messages.get(locale, "userProfile")).append("</a>")
                .append(" | ");
        footerLinks.append("<a href=\"http://go.sap.com/about/legal/impressum.html\">")
                .append(messages.get(locale, "imprint")).append("</a>")
                .append(" | ");
        footerLinks.append("<a href=\"http://go.sap.com/about/legal/privacy.html\">")
                .append(messages.get(locale, "privacy"))
                .append("</a>");

        String subscriptionInformation = messages.get(locale, "subscriptionInformation");
        return TEMPLATE //
                .replace("${title}", notificationMailTemplate.getSubject())
                .replace("${content}", bodyContent.toString())
                .replace("${subscription_information}", subscriptionInformation)
                .replace("${site}", siteLink) //
                .replace("${footer_links}", footerLinks.toString())
        ;
    }
    
    protected abstract NotificationMailTemplate getMailTemplate(T objectToNotifyAbout, Locale locale);

}