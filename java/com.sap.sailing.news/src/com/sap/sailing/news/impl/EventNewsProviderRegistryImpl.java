package com.sap.sailing.news.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.news.EventNewsProvider;
import com.sap.sailing.news.EventNewsProviderRegistry;

public class EventNewsProviderRegistryImpl implements EventNewsProviderRegistry {
    private Set<EventNewsProvider> newsProvider; 
    
    public EventNewsProviderRegistryImpl() {
        newsProvider = new HashSet<EventNewsProvider>();
    }
    
    @Override
    public boolean registerNewsProvider(EventNewsProvider provider) {
        return newsProvider.add(provider);
    }

    @Override
    public boolean deregisterNewsProvider(EventNewsProvider provider) {
        return newsProvider.remove(provider);
    }

    @Override
    public Iterable<EventNewsProvider> getEventNewsProvider() {
        return newsProvider;
    }

}
