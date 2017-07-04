package com.sap.sailing.server.notification.impl;

import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;

import com.sap.sse.common.Util.Pair;

/**
 * The class needs to implement the {@link Serializable} interface because it can become part of a
 * {@link SerializableMultipartSupplier}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class NotificationMailTemplate implements Serializable {
    private static final long serialVersionUID = 3291811210425722552L;
    private final String subject;
    private final String title;
    private final String text;
    private final String serverBaseUrl;
    private final Iterable<Pair<String, String>> labelsAndLinkUrls;
    
    @SafeVarargs
    public NotificationMailTemplate(String subject, String text, URL serverBaseUrl,
            Pair<String, String>... labelsAndLinkUrls) {
        this(subject, null, text, serverBaseUrl, labelsAndLinkUrls);
    }

    @SafeVarargs
    public NotificationMailTemplate(String subject, String title, String text, URL serverBaseUrl,
            Pair<String, String>... labelsAndLinkUrls) {
        this.subject = subject;
        this.title = title;
        this.text = text;
        this.serverBaseUrl = serverBaseUrl.toString();
        this.labelsAndLinkUrls = Arrays.asList(labelsAndLinkUrls);
    }

    public String getServerBaseUrl() {
        return serverBaseUrl;
    }

    public String getSubject() {
        return subject;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public Iterable<Pair<String, String>> getLabelsAndLinkUrls() {
        return labelsAndLinkUrls;
    }
}
