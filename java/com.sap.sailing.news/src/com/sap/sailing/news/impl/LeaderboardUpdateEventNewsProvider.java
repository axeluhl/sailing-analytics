package com.sap.sailing.news.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.news.EventNewsItem;
import com.sap.sailing.news.EventNewsProvider;
import com.sap.sse.common.TimePoint;

/**
 * An event news provider looking at leaderboard updates triggered by changes of timePointOfLatestModification 
 * @author Frank
 *
 */
public class LeaderboardUpdateEventNewsProvider implements EventNewsProvider {

    @Override
    public Collection<? extends EventNewsItem> getNews(Event event) {
        List<LeaderboardUpdateNewsItem> result = new LinkedList<>();
        Iterable<LeaderboardGroup> leaderboardGroups = event.getLeaderboardGroups();
        for(LeaderboardGroup leaderboardGroup: leaderboardGroups) {
            for(Leaderboard leaderboard: leaderboardGroup.getLeaderboards()) {
                if(!isPartOfEvent(event, leaderboard)) {
                    continue;
                }
                SettableScoreCorrection scoreCorrection = leaderboard.getScoreCorrection();
                if(scoreCorrection != null) {
                    TimePoint timePointOfLatestModification = scoreCorrection.getTimePointOfLastCorrectionsValidity();
                    if(timePointOfLatestModification != null) {
                        String displayName = leaderboard.getDisplayName() != null ?leaderboard.getDisplayName() :leaderboard.getName();
                        String boatClassName= null;
                        if(leaderboard instanceof RegattaLeaderboard) {
                            BoatClass boatClass = ((RegattaLeaderboard) leaderboard).getRegatta().getBoatClass();
                            if(boatClass != null) {
                                boatClassName = boatClass.getDisplayName();
                            }
                        }
                        result.add(new LeaderboardUpdateNewsItem(event.getId(), timePointOfLatestModification.asDate(), leaderboard.getName(), displayName, boatClassName));
                    }
                }
            }
        }
        
        Collections.sort(result);
        
        if(result.size() <= 10) {
            return result;
        }
        return result.subList(0, 11);
    }
    
    // TODO duplicate code taken from HomeServiceUtil
    private boolean isPartOfEvent(EventBase event, Leaderboard regattaEntity) {
        final CourseArea defaultCourseArea = regattaEntity.getCourseAreas();
        if(defaultCourseArea == null) {
            return true;
        }
        for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
            if(courseArea.equals(defaultCourseArea)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<? extends EventNewsItem> getNews(Event event, Date startingFrom) {
        // TODO correct implementation
        return getNews(event);
    }

    @Override
    public boolean hasNews(Event event, Date startingFrom) {
        // TODO correct implementation
        return true;
    }

    @Override
    public boolean hasNews(Event event) {
        return true;
    }

}
