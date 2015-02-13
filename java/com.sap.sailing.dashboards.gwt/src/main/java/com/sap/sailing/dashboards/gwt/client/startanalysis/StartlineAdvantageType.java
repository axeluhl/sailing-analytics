package com.sap.sailing.dashboards.gwt.client.startanalysis;

import com.sap.sailing.domain.common.LegType;

public enum StartlineAdvantageType {
    WIND("Startline Advantage by Wind", LegType.UPWIND),
    GEOMETRIC("Startline Advantage by Geometry", LegType.REACHING);

    private final String displayName;
    private final LegType firstLegType;

    private StartlineAdvantageType(String displayName, LegType firstLegType) {
        this.displayName = displayName;
        this.firstLegType = firstLegType;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    public LegType getFirstLegType() {
        return firstLegType;
    }
}
