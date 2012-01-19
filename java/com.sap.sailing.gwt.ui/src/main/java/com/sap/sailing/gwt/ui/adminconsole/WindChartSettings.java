package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.WindSource;

public class WindChartSettings {
    private final Set<WindSource> windSourcesToDisplay;

    public WindChartSettings(Set<WindSource> windSourcesToDisplay) {
        this.windSourcesToDisplay = windSourcesToDisplay;
    }
    
    public WindChartSettings(WindSource... windSourcesToDisplay) {
        this(new HashSet<WindSource>(windSourcesToDisplay==null?new ArrayList<WindSource>():Arrays.asList(windSourcesToDisplay)));
    }

    public Set<WindSource> getWindSourcesToDisplay() {
        return windSourcesToDisplay;
    }
}
