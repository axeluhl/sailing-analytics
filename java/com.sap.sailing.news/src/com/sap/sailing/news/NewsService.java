package com.sap.sailing.news;

import java.util.Date;

public interface NewsService {
    Iterable<NewsItem> getAllNews();

    Iterable<NewsItem> getNews(Date startingFrom);

    Iterable<NewsItem> getNewsByCategory(String category);

    Iterable<NewsItem> getNewsByCategory(Date startingFrom, String category);
}
