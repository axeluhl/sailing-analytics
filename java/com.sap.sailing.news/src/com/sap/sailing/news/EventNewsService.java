package com.sap.sailing.news;

import java.util.Date;
import java.util.List;

import com.sap.sailing.domain.base.Event;

public interface EventNewsService {
    List<EventNewsItem> getNews(Event event);

    List<EventNewsItem> getNews(Event event, Date startingFrom);

    List<EventNewsItem> getNewsByCategory(Event event, String category);

    List<EventNewsItem> getNewsByCategory(Event event, Date startingFrom, String category);
}
