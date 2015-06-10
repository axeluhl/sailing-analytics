package com.sap.sailing.news.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.news.NewsProvider;
import com.sap.sailing.news.NewsProviderRegistry;

public class NewsProviderRegistryImpl implements NewsProviderRegistry {
    private Set<NewsProvider> newsProvider; 
    
    public NewsProviderRegistryImpl() {
        newsProvider = new HashSet<NewsProvider>();
    }
    
    @Override
    public boolean registerNewsProvider(NewsProvider provider) {
        return newsProvider.add(provider);
    }

    @Override
    public boolean deregisterNewsProvider(NewsProvider provider) {
        return newsProvider.remove(provider);
    }

    @Override
    public Iterable<NewsProvider> getNewsProvider() {
        return newsProvider;
    }

}
