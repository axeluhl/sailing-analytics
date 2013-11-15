package com.sap.sailing.domain.base;

/**
 * Used to decided which Course Designer to pick.
 * 
 * When modifying these values also check res/preferences.xml of racecommittee.app!
 */
public enum CourseDesignerMode {
    BY_NAME,    // O2, O3, I2,...
    BY_MAP,     // Map-based Course Designer
    BY_MARKS    // ESS
}
