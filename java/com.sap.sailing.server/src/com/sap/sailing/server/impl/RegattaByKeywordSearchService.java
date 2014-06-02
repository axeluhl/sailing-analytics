package com.sap.sailing.server.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RegattaSearchResult;
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
    Result<RegattaSearchResult> search(final RacingEventService racingEventService, KeywordQuery query) {
        ResultImpl<RegattaSearchResult> result = new ResultImpl<>(query, new RegattaSearchResultRanker());
        AbstractListFilter<Regatta> regattaFilter = new AbstractListFilter<Regatta>() {
            @Override
            public Iterable<String> getStrings(Regatta regatta) {
                List<String> regattaStrings = new ArrayList<>();
                regattaStrings.add(regatta.getBaseName());
                regattaStrings.add(regatta.getBoatClass().getName());
                for (Competitor competitor : regatta.getCompetitors()) {
                    regattaStrings.add(competitor.getName());
                    regattaStrings.add(competitor.getBoat().getSailID());
                }
                final Iterable<LeaderboardGroup> leaderboardGroupsHostingRegatta = getLeaderboardGroupsHostingRegatta(regatta, racingEventService);
                for (LeaderboardGroup leaderboardGroup : leaderboardGroupsHostingRegatta) {
                    regattaStrings.add(leaderboardGroup.getName());
                    regattaStrings.add(leaderboardGroup.getDescription());
                    for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                        if (leaderboard instanceof RegattaLeaderboard && ((RegattaLeaderboard) leaderboard).getRegatta() == regatta) {
                            regattaStrings.add(leaderboard.getName());
                            regattaStrings.add(leaderboard.getDisplayName());
                            for (Competitor competitor : leaderboard.getCompetitors()) {
                                String competitorDisplayName = leaderboard.getDisplayName(competitor);
                                if (competitorDisplayName != null) {
                                    regattaStrings.add(competitorDisplayName);
                                }
                            }
                        }
                    }
                }
                for (Event event : getEventsHostingRegatta(regatta, racingEventService, leaderboardGroupsHostingRegatta)) {
                    regattaStrings.add(event.getName());
                    regattaStrings.add(event.getVenue().getName());
                }
                return regattaStrings;
            }
        };
        for (Regatta matchingRegatta : regattaFilter.applyFilter(query.getKeywords(), racingEventService.getAllRegattas())) {
            result.addHit(new RegattaSearchResultImpl(matchingRegatta));
        }
        return result;
    }

    private Iterable<LeaderboardGroup> getLeaderboardGroupsHostingRegatta(Regatta regatta, RacingEventService racingEventService) {
        Set<LeaderboardGroup> result = new LinkedHashSet<>();
        for (LeaderboardGroup leaderboardGroup : racingEventService.getLeaderboardGroups().values()) {
            for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                if (leaderboard instanceof RegattaLeaderboard && ((RegattaLeaderboard) leaderboard).getRegatta() == regatta) {
                    result.add(leaderboardGroup);
                }
            }
        }
        return result;
    }

    private Iterable<Event> getEventsHostingRegatta(Regatta regatta, RacingEventService racingEventService,
            Iterable<LeaderboardGroup> leaderboardGroupsHostingRegatta) {
        Set<Event> result = new LinkedHashSet<>();
        for (Event event : racingEventService.getAllEvents()) {
            if (Util.contains(event.getVenue().getCourseAreas(), regatta.getDefaultCourseArea())) {
                result.add(event);
            } else {
                for (LeaderboardGroup leaderboardGroupHostingRegatta : leaderboardGroupsHostingRegatta) {
                    if (Util.contains(event.getLeaderboardGroups(), leaderboardGroupHostingRegatta)) {
                        result.add(event);
                        break;
                    }
                }
            }
        }
        return result;
    }
}
