package com.sap.sailing.news;

public interface EventNewsProviderRegistry {
    boolean registerNewsProvider(EventNewsProvider provider);

    boolean deregisterNewsProvider(EventNewsProvider provider);
    
    Iterable<EventNewsProvider> getEventNewsProvider();
}
 