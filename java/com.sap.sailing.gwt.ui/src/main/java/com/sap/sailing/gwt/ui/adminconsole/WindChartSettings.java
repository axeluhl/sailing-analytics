package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Set;

import com.sap.sailing.domain.common.WindSourceType;

public class WindChartSettings {
    private final Set<WindSourceType> windSourceTypesToDisplay;
    
    private final long resolutionInMilliseconds;

    private final boolean showWindSpeedSeries;
    private final boolean showWindDirectionsSeries;

    public WindChartSettings(boolean showWindSpeedSeries, boolean showWindDirectionsSeries, Set<WindSourceType> windSourceTypesToDisplay, long resolutionInMilliseconds) {
        this.windSourceTypesToDisplay = windSourceTypesToDisplay;
        this.resolutionInMilliseconds = resolutionInMilliseconds;
        this.showWindDirectionsSeries = showWindDirectionsSeries;
        this.showWindSpeedSeries = showWindSpeedSeries;
    }
    
    /**
     * Uses {@link WindChart#DEFAULT_RESOLUTION_IN_MILLISECONDS} as resolution
     */
    public WindChartSettings(boolean showWindSpeedSeries, boolean showWindDirectionsSeries, Set<WindSourceType> windSourceTypesToDisplay) {
        this(showWindSpeedSeries, showWindDirectionsSeries, windSourceTypesToDisplay, WindChart.DEFAULT_RESOLUTION_IN_MILLISECONDS);
    }

    public Set<WindSourceType> getWindSourceTypesToDisplay() {
        return windSourceTypesToDisplay;
    }

    public long getResolutionInMilliseconds() {
        return resolutionInMilliseconds;
    }

    public boolean isShowWindSpeedSeries() {
        return showWindSpeedSeries;
    }

    public boolean isShowWindDirectionsSeries() {
        return showWindDirectionsSeries;
    }
    
}
