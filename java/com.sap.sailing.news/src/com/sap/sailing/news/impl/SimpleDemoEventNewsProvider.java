package com.sap.sailing.news.impl;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.news.EventNewsItem;
import com.sap.sailing.news.EventNewsProvider;
import com.sap.sailing.news.LimitedQueue;

public class SimpleDemoEventNewsProvider implements EventNewsProvider {
    private final LinkedList<? extends EventNewsItem> newsQueue;

    public SimpleDemoEventNewsProvider() {
        newsQueue = new LimitedQueue<>(10);
    }
    
    @Override
    public Collection<? extends EventNewsItem> getNews(Event event) {
//        Iterable<LeaderboardGroup> leaderboardGroups = event.getLeaderboardGroups();
//        for(LeaderboardGroup leaderboardGroup: leaderboardGroups) {
//            for(Leaderboard leaderboard: leaderboardGroup.getLeaderboards()) {
//                TimePoint timePointOfLatestModification = leaderboard.getTimePointOfLatestModification();
//            }
//        }
        
        return newsQueue.subList(0, newsQueue.size());
    }

    @Override
    public Collection<? extends EventNewsItem> getNews(Event event, Date startingFrom) {
        return newsQueue.subList(0, newsQueue.size());
    }

    @Override
    public boolean hasNews(Event event, Date startingFrom) {
        return newsQueue.size() > 0;
    }

    @Override
    public boolean hasNews(Event event) {
        return newsQueue.size() > 0;
    }

}
