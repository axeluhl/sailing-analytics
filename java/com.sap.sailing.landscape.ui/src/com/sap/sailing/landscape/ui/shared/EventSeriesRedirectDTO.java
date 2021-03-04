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
    @Deprecated
    EventSeriesRedirectDTO() {} // for GWT RPC only

    public EventSeriesRedirectDTO(UUID id) {
        super(id);
    }
    
    @Override
    public Optional<String> getQuery() {
        return Optional.of("#/series/:leaderboardGroupId="+getId().toString());
    }
}
