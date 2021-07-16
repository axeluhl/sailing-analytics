package com.sap.sailing.landscape.ui.shared;

import java.util.Optional;
import java.util.UUID;

/**
 * Redirects to a specific event serie's landing page. The ID is that of the {@code LeaderboardGroup} representing
 * the event series, where the {@code LeaderboardGroup} is expected to have an overall leaderboard for the seasonal
 * scoring, indicative of this being an event series.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class EventSeriesRedirectDTO extends RedirectWithIdDTO {
    // TODO _=_ is a workaround only; see https://console.aws.amazon.com/support/home#/case/?displayId=8094019001&language=en
    private static final String QUERY_PREFIX = "_=_&#{query}#/series/:leaderboardGroupId=";

    @Deprecated
    EventSeriesRedirectDTO() {} // for GWT RPC only

    public EventSeriesRedirectDTO(UUID id) {
        super(id, Type.EVENT_SERIES);
    }
    
    @Override
    public Optional<String> getQuery() {
        return Optional.of(QUERY_PREFIX+getId().toString());
    }

    static EventSeriesRedirectDTO parse(String redirectPath) {
        return parse(redirectPath, QUERY_PREFIX, EventSeriesRedirectDTO::new);
    }
}
