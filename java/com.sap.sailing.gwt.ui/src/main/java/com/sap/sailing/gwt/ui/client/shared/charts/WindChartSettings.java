package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.LinkedHashSet;
import java.util.Set;

import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sse.common.settings.AbstractSettings;

public class WindChartSettings extends AbstractSettings {
    public static final long DEFAULT_RESOLUTION_IN_MILLISECONDS = 10000;

    private final Set<WindSourceType> windDirectionSourcesToDisplay;
    private final Set<WindSourceType> windSpeedSourcesToDisplay;
    
    private long resolutionInMilliseconds;

    private boolean showWindSpeedSeries;
    private boolean showWindDirectionsSeries;

    /**
     *  The default settings
     */
    public WindChartSettings() {
        showWindSpeedSeries = true;
        windSpeedSourcesToDisplay = new LinkedHashSet<WindSourceType>();
        windSpeedSourcesToDisplay.add(WindSourceType.COMBINED);
        showWindDirectionsSeries = true;
        windDirectionSourcesToDisplay = new LinkedHashSet<WindSourceType>();
        windDirectionSourcesToDisplay.add(WindSourceType.COMBINED);
        resolutionInMilliseconds = DEFAULT_RESOLUTION_IN_MILLISECONDS;
    }
    
    public WindChartSettings(boolean showWindSpeedSeries, Set<WindSourceType> windSpeedSourcesToDisplay, 
            boolean showWindDirectionsSeries, Set<WindSourceType> windDirectionSourcesToDisplay, long resolutionInMilliseconds) {
        this.showWindSpeedSeries = showWindSpeedSeries;
        this.windSpeedSourcesToDisplay = windSpeedSourcesToDisplay;
        this.showWindDirectionsSeries = showWindDirectionsSeries;
        this.windDirectionSourcesToDisplay = windDirectionSourcesToDisplay;
        this.resolutionInMilliseconds = resolutionInMilliseconds;
    }
    
    /**
     * Uses {@link WindChart#DEFAULT_RESOLUTION_IN_MILLISECONDS} as resolution
     */
    public WindChartSettings(boolean showWindSpeedSeries, Set<WindSourceType> windSpeedSourcesToDisplay, 
            boolean showWindDirectionsSeries, Set<WindSourceType> windDirectionSourcesToDisplay) {
        this(showWindSpeedSeries, windSpeedSourcesToDisplay, showWindDirectionsSeries, windDirectionSourcesToDisplay, DEFAULT_RESOLUTION_IN_MILLISECONDS);
    }

    public Set<WindSourceType> getWindDirectionSourcesToDisplay() {
        return windDirectionSourcesToDisplay;
    }

    public Set<WindSourceType> getWindSpeedSourcesToDisplay() {
        return windSpeedSourcesToDisplay;
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

    public void setResolutionInMilliseconds(long resolutionInMilliseconds) {
        this.resolutionInMilliseconds = resolutionInMilliseconds;
    }

    public void setShowWindSpeedSeries(boolean showWindSpeedSeries) {
        this.showWindSpeedSeries = showWindSpeedSeries;
    }

    public void setShowWindDirectionsSeries(boolean showWindDirectionsSeries) {
        this.showWindDirectionsSeries = showWindDirectionsSeries;
    }

    public void setWindDirectionSourcesToDisplay(Set<WindSourceType> windDirectionSourcesToDisplay) {
        if(windDirectionSourcesToDisplay != null) {
            this.windDirectionSourcesToDisplay.clear();
            this.windDirectionSourcesToDisplay.addAll(windDirectionSourcesToDisplay);
        }
    }

    public void setWindSpeedSourcesToDisplay(Set<WindSourceType> windSpeedSourcesToDisplay) {
        if(windSpeedSourcesToDisplay != null) {
            this.windSpeedSourcesToDisplay.clear();
            this.windSpeedSourcesToDisplay.addAll(windSpeedSourcesToDisplay);
        }
    }
}
