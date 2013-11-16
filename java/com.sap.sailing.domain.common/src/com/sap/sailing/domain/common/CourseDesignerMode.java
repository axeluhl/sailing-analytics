package com.sap.sailing.domain.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to decided which Course Designer to pick.
 * 
 * When modifying these values also check res/preferences.xml of racecommittee.app!
 */
public enum CourseDesignerMode {
    UNKNOWN,
    BY_NAME,    // O2, O3, I2,...
    BY_MAP,     // Map-based Course Designer
    BY_MARKS;    // ESS
    
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
