package com.sap.sailing.news;

public interface NewsProviderRegistry {
    boolean registerNewsProvider(NewsProvider provider);

    boolean deregisterNewsProvider(NewsProvider provider);
    
    Iterable<NewsProvider> getNewsProvider();
}
 