package com.sap.sailing.news.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import com.sap.sailing.news.LimitedQueue;
import com.sap.sailing.news.NewsItem;
import com.sap.sailing.news.NewsProvider;

public class SimpleDemoNewsProvider implements NewsProvider {
    private final LinkedList<NewsItem> newsQueue;

    public SimpleDemoNewsProvider() {
        newsQueue = new LimitedQueue<NewsItem>(10);
    }
    
    @Override
    public Iterator<NewsItem> getNews() {
        return newsQueue.iterator();
    }

    @Override
    public Iterator<NewsItem> getNews(Date startingFrom) {
        return newsQueue.iterator();
    }

    @Override
    public boolean hasNews(Date startingFrom) {
        return newsQueue.size() > 0;
    }

    @Override
    public boolean hasNews() {
        return newsQueue.size() > 0;
    }

}
