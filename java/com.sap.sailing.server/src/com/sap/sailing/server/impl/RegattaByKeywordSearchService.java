package com.sap.sailing.server.impl;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RegattaSearchResult;
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
    Result<RegattaSearchResult> search(RacingEventService service, KeywordQuery query) {
        ResultImpl<RegattaSearchResult> result = new ResultImpl<>(query, new RegattaSearchResultRanker());
        for (Regatta regatta : service.getAllRegattas()) {
            // TODO continue here...
        }
        return result;
    }
}
