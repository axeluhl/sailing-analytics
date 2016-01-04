package com.sap.sailing.news;

import java.util.Collection;
import java.util.Date;

import com.sap.sailing.domain.base.Event;

public interface EventNewsProvider {
    Collection<? extends EventNewsItem> getNews(Event event);
    
    Collection<? extends EventNewsItem> getNews(Event event, Date startingFrom);

    boolean hasNews(Event event, Date startingFrom);

    boolean hasNews(Event event);
}
