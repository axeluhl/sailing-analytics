package com.sap.sailing.news.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.news.EventNewsItem;
import com.sap.sailing.news.EventNewsProvider;
import com.sap.sailing.news.EventNewsProviderRegistry;
import com.sap.sailing.news.EventNewsService;

public class EventNewsServiceImpl implements EventNewsService {
    private final EventNewsProviderRegistry providerRegistry;
    
    public EventNewsServiceImpl(EventNewsProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
    }

    @Override
    public List<EventNewsItem> getNews(Event event) {
        List<EventNewsItem> result = new ArrayList<>();
        for(EventNewsProvider provider: providerRegistry.getEventNewsProvider()) {
            Collection<? extends EventNewsItem> news = provider.getNews(event);
            result.addAll(news);
        }
        return result;
    }

    @Override
    public List<EventNewsItem> getNews(Event event, Date startingFrom) {
        List<EventNewsItem> result = new ArrayList<>();
        for(EventNewsProvider provider: providerRegistry.getEventNewsProvider()) {
            Collection<? extends EventNewsItem> news = provider.getNews(event, startingFrom);
            result.addAll(news);
        }
        return result;
    }

    @Override
    public List<EventNewsItem> getNewsByCategory(Event event, String category) {
        return getNews(event);
    }

    @Override
    public List<EventNewsItem> getNewsByCategory(Event event, Date startingFrom, String category) {
        return getNews(event, startingFrom);
    }

}
