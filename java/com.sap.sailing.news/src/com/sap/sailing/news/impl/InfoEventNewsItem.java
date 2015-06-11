package com.sap.sailing.news.impl;

import java.net.URL;
import java.util.Date;
import java.util.UUID;

public class InfoEventNewsItem extends AbstractEventNewsItem {
    public InfoEventNewsItem(UUID eventId, String title, String message, Date createdAtDate, String category,
            URL relatedItemLink) {
        super(eventId, title, message, createdAtDate, category, relatedItemLink);
    }

    public InfoEventNewsItem(UUID eventId, String title, String message, Date createdAtDate, String category) {
        super(eventId, title, message, createdAtDate, category);
    }

}
