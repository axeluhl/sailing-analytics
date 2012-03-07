package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.WindSourceType;

public class WindChartSettings {
    private final Set<WindSourceType> windSourceTypesToDisplay;

    public WindChartSettings(Set<WindSourceType> windSourceTypesToDisplay) {
        this.windSourceTypesToDisplay = windSourceTypesToDisplay;
    }
    
    public WindChartSettings(WindSourceType... windSourceTypesToDisplay) {
        this(new HashSet<WindSourceType>(windSourceTypesToDisplay==null?new ArrayList<WindSourceType>():Arrays.asList(windSourceTypesToDisplay)));
    }

    public Set<WindSourceType> getWindSourceTypesToDisplay() {
        return windSourceTypesToDisplay;
    }
}
