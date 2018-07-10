package com.sap.sailing.gwt.home.shared.places.fakeseries;

import com.sap.sailing.gwt.home.desktop.places.fakeseries.EventSeriesAnalyticsDataManager;

/**
 * Common context used by the different tabs in the series place.
 * 
 */
public class SeriesContext {

    private final String seriesId;
    private final String leaderboardGroupName;
    
    private EventSeriesAnalyticsDataManager analyticsManager;

    public SeriesContext(String seriesId, String leaderboardGroupName) {
        this.seriesId = seriesId;
        this.leaderboardGroupName = leaderboardGroupName;
    }

    public SeriesContext(SeriesContext ctx) {
        this.seriesId = ctx.seriesId;
        this.leaderboardGroupName = ctx.leaderboardGroupName;
        withAnalyticsManager(ctx.analyticsManager);
    }
    
    public String getLeaderboardGroupName() {
        return leaderboardGroupName;
    }

    public String getSeriesId() {
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
