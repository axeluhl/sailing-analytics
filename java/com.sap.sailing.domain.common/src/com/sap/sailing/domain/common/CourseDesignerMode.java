package com.sap.sailing.domain.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to decided which Course Designer to pick.
 * 
 * When modifying these values also check res/preferences.xml of racecommittee.app!
 */
public enum CourseDesignerMode {
    UNKNOWN("Unknown", /* waypointSequenceValid */ false),
    BY_NAME("By-Name Course Designer", /* waypointSequenceValid */ false),        // O2, O3, I2,...
    BY_MAP("By-Map Course Designer", /* waypointSequenceValid */ true),           // Map-based Course Designer
    BY_MARKS("By-Marks Course Designer", /* waypointSequenceValid */ true),       // ESS
    ADMIN_CONSOLE("Administration Console", /* waypointSequenceValid */ true);
    
    private final String displayName;
    
    /**
     * Some course designers may only be able to tell, say, a name for the new course but not
     * necessarily a valid waypoint sequence. This flag tells.
     */
    private final boolean waypointSequenceValid;

    private CourseDesignerMode(String displayName, boolean waypointSequenceValid) {
        this.displayName = displayName;
        this.waypointSequenceValid = waypointSequenceValid;
    }

    @Override 
    public String toString() {
        return displayName;
    }
    
    public boolean isWaypointSequenceValid() {
        return waypointSequenceValid;
    }

    public static CourseDesignerMode[] validValues() {
        List<CourseDesignerMode> validValues = new ArrayList<CourseDesignerMode>();
        for (CourseDesignerMode type : values()) {
            if (type != UNKNOWN && type != ADMIN_CONSOLE) {
                validValues.add(type);
            }
        }
        return validValues.toArray(new CourseDesignerMode[0]);
    }
}
