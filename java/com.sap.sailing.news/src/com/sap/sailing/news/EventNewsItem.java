package com.sap.sailing.news;

import java.util.UUID;

/**
 * An interface for event related news item
 * @author Frank
 *
 */
public interface EventNewsItem extends NewsItem {
    UUID getEventUUID();
}
