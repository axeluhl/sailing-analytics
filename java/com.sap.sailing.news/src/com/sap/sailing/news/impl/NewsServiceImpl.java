package com.sap.sailing.news.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.news.NewsItem;
import com.sap.sailing.news.NewsProvider;
import com.sap.sailing.news.NewsProviderRegistry;
import com.sap.sailing.news.NewsService;
import com.sap.sse.common.Util;

public class NewsServiceImpl implements NewsService {
    private final NewsProviderRegistry providerRegistry;
    
    public NewsServiceImpl(NewsProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
    }

    @Override
    public Iterable<NewsItem> getAllNews() {
        List<NewsItem> result = new ArrayList<>();
        for(NewsProvider provider: providerRegistry.getNewsProvider()) {
            Iterator<NewsItem> news = provider.getNews();
        }
        return result;
    }

    @Override
    public Iterable<NewsItem> getNews(Date startingFrom) {
        List<NewsItem> result = new ArrayList<>();
        for(NewsProvider provider: providerRegistry.getNewsProvider()) {
            Iterator<NewsItem> news = provider.getNews(startingFrom);
        }
        return result;
    }

    @Override
    public Iterable<NewsItem> getNewsByCategory(String category) {
        return getAllNews();
    }

    @Override
    public Iterable<NewsItem> getNewsByCategory(Date startingFrom, String category) {
        return getNews(startingFrom);
    }

}
