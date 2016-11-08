package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.LinkedHashSet;
import java.util.Set;

import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.BooleanSetting;

public class WindChartSettings extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = -3250243915670349222L;

    public static final long DEFAULT_RESOLUTION_IN_MILLISECONDS = 10000;

    private final Set<WindSourceType> windDirectionSourcesToDisplay;
    private final Set<WindSourceType> windSpeedSourcesToDisplay;
    
    private long resolutionInMilliseconds;

    private BooleanSetting showWindSpeedSeries;
    private BooleanSetting showWindDirectionsSeries;
    
    @Override
    protected void addChildSettings() {
        showWindSpeedSeries = new BooleanSetting("showWindSpeedSeries", this, true);
        showWindDirectionsSeries = new BooleanSetting("showWindDirectionsSeries", this, true);
    }

    /**
     *  The default settings
     */
    public WindChartSettings() {
        windSpeedSourcesToDisplay = new LinkedHashSet<WindSourceType>();
        windSpeedSourcesToDisplay.add(WindSourceType.COMBINED);
        windDirectionSourcesToDisplay = new LinkedHashSet<WindSourceType>();
        windDirectionSourcesToDisplay.add(WindSourceType.COMBINED);
        resolutionInMilliseconds = DEFAULT_RESOLUTION_IN_MILLISECONDS;
    }
    
    public WindChartSettings(boolean showWindSpeedSeries, Set<WindSourceType> windSpeedSourcesToDisplay, 
            boolean showWindDirectionsSeries, Set<WindSourceType> windDirectionSourcesToDisplay, long resolutionInMilliseconds) {
        this.showWindSpeedSeries.setValue(showWindSpeedSeries);
        this.windSpeedSourcesToDisplay = windSpeedSourcesToDisplay;
        this.showWindDirectionsSeries.setValue(showWindDirectionsSeries);
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
        return showWindSpeedSeries.getValue();
    }

    public boolean isShowWindDirectionsSeries() {
        return showWindDirectionsSeries.getValue();
    }

    public void setResolutionInMilliseconds(long resolutionInMilliseconds) {
        this.resolutionInMilliseconds = resolutionInMilliseconds;
    }

    public void setShowWindSpeedSeries(boolean showWindSpeedSeries) {
        this.showWindSpeedSeries.setValue(showWindSpeedSeries);
    }

    public void setShowWindDirectionsSeries(boolean showWindDirectionsSeries) {
        this.showWindDirectionsSeries.setValue(showWindDirectionsSeries);
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
