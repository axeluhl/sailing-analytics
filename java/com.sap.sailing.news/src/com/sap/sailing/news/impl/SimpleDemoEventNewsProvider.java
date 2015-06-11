package com.sap.sailing.news.impl;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.news.EventNewsItem;
import com.sap.sailing.news.EventNewsProvider;
import com.sap.sailing.news.LimitedQueue;

public class SimpleDemoEventNewsProvider implements EventNewsProvider {
    private final LinkedList<EventNewsItem> newsQueue;
    
    private final Timer timer = new Timer();

    public SimpleDemoEventNewsProvider() {
        newsQueue = new LimitedQueue<>(10);
        
        final UUID eventId = UUID.fromString("406cffc2-b491-4f7e-b78e-a21abd97006a");
//        final UUID eventId = UUID.fromString("406cffc2-b491-4f7e-b78e-a21abd97006a");
        
        timer.scheduleAtFixedRate(new TimerTask() {
            int counter=1;
            @Override
            public void run() {
                newsQueue.add(new InfoEventNewsItem(eventId, "Test Entry " + counter, "Content for Test Entry "+counter, new Date(), null));
                counter++;
            }
        }, 5_000, 30_000);
    }
    
    protected void finalize() throws Throwable {
        timer.cancel();
    };
    
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
