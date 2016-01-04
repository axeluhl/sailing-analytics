package com.sap.sailing.news;

import java.net.URL;
import java.util.Date;
import java.util.UUID;

/**
 * An interface for a generic news item
 * @author Frank
 *
 */
public interface NewsItem extends Comparable<NewsItem> {
    UUID getId();
    
    String getCategory();

    String getTitle();
    
    String getMessage();
    
    URL getRelatedItemLink();
    
    Date getCreatedAtDate();
}
