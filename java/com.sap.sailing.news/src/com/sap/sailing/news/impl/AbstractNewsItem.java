package com.sap.sailing.news.impl;

import java.net.URL;
import java.util.Date;
import java.util.UUID;

import com.sap.sailing.news.NewsItem;

public class AbstractNewsItem implements NewsItem {
    private final UUID id;
    
    private String category;

    private String title;
    
    private String message;
    
    private URL relatedItemLink;
    
    private Date createdAtDate;

    public AbstractNewsItem(String title, String message, Date createdAtDate,  String category) {
        this(title, message, createdAtDate, category, null);
    }

    public AbstractNewsItem(String title, String message, Date createdAtDate, String category, URL relatedItemLink) {
        this.title = title;
        this.message = message;
        this.category = category;
        this.createdAtDate = createdAtDate;
        this.relatedItemLink = relatedItemLink;
        this.id = UUID.randomUUID();
    }

    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public URL getRelatedItemLink() {
        return relatedItemLink;
    }

    public Date getCreatedAtDate() {
        return createdAtDate;
    }

    public UUID getId() {
        return id;
    }
}
