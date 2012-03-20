package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.WindSourceType;

public class WindChartSettings {
    private final Set<WindSourceType> windSourceTypesToDisplay;
    
    private final long resolutionInMilliseconds;

    public WindChartSettings(Set<WindSourceType> windSourceTypesToDisplay, long resolutionInMilliseconds) {
        this.windSourceTypesToDisplay = windSourceTypesToDisplay;
        this.resolutionInMilliseconds = resolutionInMilliseconds;
    }
    
    /**
     * Uses {@link WindChart#DEFAULT_RESOLUTION_IN_MILLISECONDS} as resolution
     */
    public WindChartSettings(WindSourceType... windSourceTypesToDisplay) {
        this(WindChart.DEFAULT_RESOLUTION_IN_MILLISECONDS, windSourceTypesToDisplay);
    }

    public WindChartSettings(long resolutionInMilliseconds, WindSourceType... windSourceTypesToDisplay) {
        this(new HashSet<WindSourceType>(windSourceTypesToDisplay == null ? new ArrayList<WindSourceType>()
                : Arrays.asList(windSourceTypesToDisplay)), resolutionInMilliseconds);
    }

    public Set<WindSourceType> getWindSourceTypesToDisplay() {
        return windSourceTypesToDisplay;
    }

    public long getResolutionInMilliseconds() {
        return resolutionInMilliseconds;
    }
    
}
