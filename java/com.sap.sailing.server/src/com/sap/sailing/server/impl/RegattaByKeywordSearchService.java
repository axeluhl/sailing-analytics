package com.sap.sailing.server.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.LeaderboardSearchResult;
import com.sap.sailing.domain.base.impl.LeaderboardSearchResultImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.AbstractListFilter;
import com.sap.sse.common.search.KeywordQuery;
import com.sap.sse.common.search.Result;
import com.sap.sse.common.search.ResultImpl;

/**
 * Searches a {@link RacingEventService} instance for regattas that somehow match with a
 * {@link KeywordQuery}. Several attributes on the way are considered, in particular the event
 * name (if a regatta is somehow linked to an event), the event's venue name, the leaderboard group
 * name that contains a regatta leaderboard for the subject regatta, the regatta name itself,
 * the regatta's boat class name, and the names of all competitors entered into the regatta.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RegattaByKeywordSearchService {
    Result<LeaderboardSearchResult> search(final RacingEventService racingEventService, KeywordQuery query) {
        ResultImpl<LeaderboardSearchResult> result = new ResultImpl<>(query, new LeaderboardSearchResultRanker(racingEventService));
        final Map<LeaderboardGroup, Set<Event>> eventsForLeaderboardGroup = new HashMap<>();
        final Map<Leaderboard, Set<LeaderboardGroup>> leaderboardGroupsForLeaderboard = new HashMap<>();
        final Map<CourseArea, Event> eventForCourseArea = new HashMap<>();
        final Map<Event, Set<String>> stringsForEvent = new HashMap<>();
        final Map<LeaderboardGroup, Set<String>> stringsForLeaderboardGroup = new HashMap<>();
        for (final Event event : racingEventService.getAllEvents()) {
            final Set<String> s4e = new HashSet<>();
            s4e.add(event.getName());
            s4e.add(event.getVenue().getName());
            stringsForEvent.put(event, s4e);
            for (final LeaderboardGroup leaderboardGroup : event.getLeaderboardGroups()) {
                Util.add(eventsForLeaderboardGroup, leaderboardGroup, event);
            }
            for (final CourseArea courseArea : event.getVenue().getCourseAreas()) {
                eventForCourseArea.put(courseArea, event);
            }
        }
        for (final LeaderboardGroup leaderboardGroup : racingEventService.getLeaderboardGroups().values()) {
            final Set<String> s4lg = new HashSet<>();
            s4lg.add(leaderboardGroup.getName());
            s4lg.add(leaderboardGroup.getDescription());
            stringsForLeaderboardGroup.put(leaderboardGroup, s4lg);
            for (final Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                Util.add(leaderboardGroupsForLeaderboard, leaderboard, leaderboardGroup);
            }
        }
        AbstractListFilter<Leaderboard> leaderboardFilter = new AbstractListFilter<Leaderboard>() {
            @Override
            public Iterable<String> getStrings(Leaderboard leaderboard) {
                // TODO allow recording which part of the leaderboard was matched by the keywords by returning "annotated strings" that the matcher can understand
                List<String> leaderboardStrings = new ArrayList<>();
                leaderboardStrings.add(leaderboard.getName());
                leaderboardStrings.add(leaderboard.getDisplayName());
                for (Competitor competitor : leaderboard.getCompetitors()) {
                    leaderboardStrings.add(competitor.getName());
                    leaderboardStrings.add(competitor.getBoat().getSailID());
                    String competitorDisplayName = leaderboard.getDisplayName(competitor);
                    if (competitorDisplayName != null) {
                        leaderboardStrings.add(competitorDisplayName);
                    }
                }
                final Iterable<LeaderboardGroup> leaderboardGroupsHostingLeaderboard = leaderboardGroupsForLeaderboard.get(leaderboard);
                if (leaderboardGroupsHostingLeaderboard != null) {
                    for (LeaderboardGroup leaderboardGroup : leaderboardGroupsHostingLeaderboard) {
                        leaderboardStrings.addAll(stringsForLeaderboardGroup.get(leaderboardGroup));
                        final Set<Event> eventsForLG = filterEventsForLeaderboard(leaderboard, leaderboardGroup, eventsForLeaderboardGroup.get(leaderboardGroup));
                        
                        if(leaderboardGroup.hasOverallLeaderboard() && leaderboard.getDefaultCourseArea() != null) {
                            
                        }
                        if (eventsForLG != null) {
                            for (final Event event : eventsForLG) {
                                leaderboardStrings.addAll(stringsForEvent.get(event));
                            }
                        }
                    }
                }
                final Event eventByDefaultCourseArea = eventForCourseArea.get(leaderboard.getDefaultCourseArea());
                if (eventByDefaultCourseArea != null) {
                    leaderboardStrings.addAll(stringsForEvent.get(eventByDefaultCourseArea));
                }
                return leaderboardStrings;
            }
        };
        for (Leaderboard matchingLeaderboard : leaderboardFilter.applyFilter(query.getKeywords(), racingEventService.getLeaderboards().values())) {
            result.addHit(new LeaderboardSearchResultImpl(matchingLeaderboard,
                    getEventsForLeaderboard(matchingLeaderboard, leaderboardGroupsForLeaderboard,
                            eventsForLeaderboardGroup, eventForCourseArea), leaderboardGroupsForLeaderboard
                            .get(matchingLeaderboard)));
        }
        return result;
    }
    
    private Set<Event> filterEventsForLeaderboard(Leaderboard leaderboard, LeaderboardGroup leaderboardGroup, Set<Event> events) {
        if(leaderboardGroup.hasOverallLeaderboard()) {
            CourseArea defaultCourseArea = leaderboard.getDefaultCourseArea();
            if(defaultCourseArea != null) {
                for (Event event : events) {
                    if(Util.contains(event.getVenue().getCourseAreas(), defaultCourseArea)) {
                        return Collections.singleton(event);
                    }
                }
            }
        }
        return events;
    }

    private Set<Event> getEventsForLeaderboard(Leaderboard matchingLeaderboard,
            Map<Leaderboard, Set<LeaderboardGroup>> leaderboardGroupsForLeaderboard,
            Map<LeaderboardGroup, Set<Event>> eventsForLeaderboardGroup, Map<CourseArea, Event> eventForCourseArea) {
        final Set<Event> result = new HashSet<>();
        final Set<LeaderboardGroup> lgs = leaderboardGroupsForLeaderboard.get(matchingLeaderboard);
        if (lgs != null) {
            for (final LeaderboardGroup lg : lgs) {
                final Set<Event> eventsForLG = filterEventsForLeaderboard(matchingLeaderboard, lg, eventsForLeaderboardGroup.get(lg));
                if (eventsForLG != null) {
                    result.addAll(eventsForLG);
                }
            }
        }
        final Event eventByCourseArea = eventForCourseArea.get(matchingLeaderboard.getDefaultCourseArea());
        if (eventByCourseArea != null) {
            result.add(eventByCourseArea);
        }
        return result;
    }
}
