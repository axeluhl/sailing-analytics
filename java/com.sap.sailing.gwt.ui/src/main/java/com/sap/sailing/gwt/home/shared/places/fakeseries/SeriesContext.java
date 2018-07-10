package com.sap.sailing.gwt.home.shared.places.fakeseries;

import java.util.UUID;

import com.sap.sailing.gwt.home.desktop.places.fakeseries.EventSeriesAnalyticsDataManager;

/**
 * Common context used by the different tabs in the series place.
 * 
 */
public class SeriesContext {
    private final UUID seriesId;
    private final UUID leaderboardGroupId;
    
    private EventSeriesAnalyticsDataManager analyticsManager;

    public SeriesContext(UUID seriesId, UUID leaderboardGroupId) {
        this.seriesId = seriesId;
        this.leaderboardGroupId = leaderboardGroupId;
    }

    public SeriesContext(SeriesContext ctx) {
        this.seriesId = ctx.seriesId;
        this.leaderboardGroupId = ctx.leaderboardGroupId;
        withAnalyticsManager(ctx.analyticsManager);
    }
    
    public UUID getLeaderboardGroupName() {
        return leaderboardGroupId;
    }

    public UUID getSeriesId() {
        return seriesId;
    }
    
    public EventSeriesAnalyticsDataManager getAnalyticsManager() {
        return analyticsManager;
    }

    public SeriesContext withAnalyticsManager(EventSeriesAnalyticsDataManager regattaAnalyticsManager) {
        this.analyticsManager = regattaAnalyticsManager;
        return this;
    }
}
