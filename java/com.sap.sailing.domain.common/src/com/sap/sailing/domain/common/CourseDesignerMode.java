package com.sap.sailing.domain.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to decided which Course Designer to pick.
 * 
 * When modifying these values also check res/preferences.xml of racecommittee.app!
 */
public enum CourseDesignerMode {
    UNKNOWN("Unknown"),
    BY_NAME("By-Name Course Designer"),         // O2, O3, I2,...
    BY_MAP("By-Map Course Designer"),           // Map-based Course Designer
    BY_MARKS("By-Marks Course Designer");       // ESS
    
    private String displayName;

    private CourseDesignerMode(String displayName) {
        this.displayName = displayName;
    }

    @Override 
    public String toString() {
        return displayName;
    }
    
    public static CourseDesignerMode[] validValues() {
        List<CourseDesignerMode> validValues = new ArrayList<CourseDesignerMode>();
        for (CourseDesignerMode type : values()) {
            if (type != UNKNOWN) {
                validValues.add(type);
            }
        }
        return validValues.toArray(new CourseDesignerMode[0]);
    }
}
