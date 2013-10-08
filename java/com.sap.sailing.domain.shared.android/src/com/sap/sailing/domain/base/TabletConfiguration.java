package com.sap.sailing.domain.base;

import java.util.Set;

/**
 * Interface holding information about tablet's (RCApp) configuration.
 */
public interface TabletConfiguration {
    Set<String> getAllowedCourseAreaNames();
}
