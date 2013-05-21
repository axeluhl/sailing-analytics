package com.sap.sailing.gwt.ui.shared.windpattern;

public enum WindPattern {

    NONE("Choose a wind pattern"), OSCILLATIONS("Oscillations"), OSCILLATION_WITH_BLASTS("Oscillation with Gusts"), BLASTS(
            "Gusts"), MEASURED("Measured");
 
    private String displayName;

    WindPattern(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
