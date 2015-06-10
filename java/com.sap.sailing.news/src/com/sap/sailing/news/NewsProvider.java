package com.sap.sailing.news;

import java.util.Date;
import java.util.Iterator;

public interface NewsProvider {
    Iterator<NewsItem> getNews();
    
    Iterator<NewsItem> getNews(Date startingFrom);

    boolean hasNews(Date startingFrom);

    boolean hasNews();
}
