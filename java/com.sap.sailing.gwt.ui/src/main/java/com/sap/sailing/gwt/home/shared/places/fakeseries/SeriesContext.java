package com.sap.sailing.gwt.home.shared.places.fakeseries;

import java.util.UUID;

import com.sap.sailing.gwt.home.desktop.places.fakeseries.EventSeriesAnalyticsDataManager;

/**
 * Common context used by the different tabs in the series place.
 * 
 */
public class SeriesContext {

    private final String seriesId;
    private EventSeriesAnalyticsDataManager analyticsManager;

    public SeriesContext(String seriesId) {
        this.seriesId = seriesId;
    }

    public SeriesContext(SeriesContext ctx) {
        this.seriesId = ctx.seriesId;
        withAnalyticsManager(ctx.analyticsManager);
    }

    public String getSeriesId() {
        return seriesId;
    }
    
    public UUID getSeriesUUID() {
        if(seriesId == null) {
            // TODO assert because seriesId is required
            return null;
        }
        return UUID.fromString(seriesId);
    }
    
    public EventSeriesAnalyticsDataManager getAnalyticsManager() {
        return analyticsManager;
    }

    public SeriesContext withAnalyticsManager(EventSeriesAnalyticsDataManager regattaAnalyticsManager) {
        this.analyticsManager = regattaAnalyticsManager;
        return this;
    }
}
