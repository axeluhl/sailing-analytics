package com.sap.sailing.server.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.LeaderboardSearchResult;
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
        ResultImpl<LeaderboardSearchResult> result = new ResultImpl<>(query, new RegattaSearchResultRanker(racingEventService));
        AbstractListFilter<Leaderboard> leaderboardFilter = new AbstractListFilter<Leaderboard>() {
            @Override
            public Iterable<String> getStrings(Leaderboard leaderboard) {
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
                final Iterable<LeaderboardGroup> leaderboardGroupsHostingLeaderboard = getLeaderboardGroupsHostingLeaderboard(
                        leaderboard, racingEventService);
                for (LeaderboardGroup leaderboardGroup : leaderboardGroupsHostingLeaderboard) {
                    leaderboardStrings.add(leaderboardGroup.getName());
                    leaderboardStrings.add(leaderboardGroup.getDescription());
                }
                for (Event event : getEventsHostingLeaderboard(leaderboard, racingEventService, leaderboardGroupsHostingLeaderboard)) {
                    leaderboardStrings.add(event.getName());
                    leaderboardStrings.add(event.getVenue().getName());
                }
                return leaderboardStrings;
            }
        };
        for (Leaderboard matchingLeaderboard : leaderboardFilter.applyFilter(query.getKeywords(), racingEventService.getLeaderboards().values())) {
            result.addHit(new LeaderboardSearchResultImpl(matchingLeaderboard));
        }
        return result;
    }

    private Iterable<LeaderboardGroup> getLeaderboardGroupsHostingLeaderboard(Leaderboard leaderboard, RacingEventService racingEventService) {
        Set<LeaderboardGroup> result = new LinkedHashSet<>();
        for (LeaderboardGroup leaderboardGroup : racingEventService.getLeaderboardGroups().values()) {
            for (Leaderboard lgLeaderboard : leaderboardGroup.getLeaderboards()) {
                if (lgLeaderboard == leaderboard) {
                    result.add(leaderboardGroup);
                }
            }
        }
        return result;
    }

    private Iterable<Event> getEventsHostingLeaderboard(Leaderboard leaderboard, RacingEventService racingEventService,
            Iterable<LeaderboardGroup> leaderboardGroupsHostingLeaderboard) {
        Set<Event> result = new LinkedHashSet<>();
        for (Event event : racingEventService.getAllEvents()) {
            if (Util.contains(event.getVenue().getCourseAreas(), leaderboard.getDefaultCourseArea())) {
                result.add(event);
            } else {
                for (LeaderboardGroup leaderboardGroupHostingRegatta : leaderboardGroupsHostingLeaderboard) {
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
