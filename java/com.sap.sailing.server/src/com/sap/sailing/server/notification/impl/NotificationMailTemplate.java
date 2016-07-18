package com.sap.sailing.server.notification.impl;

import java.util.Arrays;

import com.sap.sse.common.Util.Pair;

public class NotificationMailTemplate {
  
    private final String subject;
    private final String title;
    private final String text;
    private final Iterable<Pair<String, String>> labelsAndLinkUrls;
    
    @SafeVarargs
    public NotificationMailTemplate(String subject, String text, Pair<String, String>... labelsAndLinkUrls) {
        this(subject, null, text, labelsAndLinkUrls);
    }

    @SafeVarargs
    public NotificationMailTemplate(String subject, String title, String text, Pair<String, String>... labelsAndLinkUrls) {
        this.subject = subject;
        this.title = title;
        this.text = text;
        this.labelsAndLinkUrls = Arrays.asList(labelsAndLinkUrls);
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
