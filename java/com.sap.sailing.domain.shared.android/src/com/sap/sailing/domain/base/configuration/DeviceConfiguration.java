package com.sap.sailing.domain.base.configuration;

import java.util.List;

/**
 * Interface holding information about tablet's (RCApp) configuration.
 */
public interface DeviceConfiguration {
    List<String> getAllowedCourseAreaNames();
    Integer getMinimumRoundsForCourse();
    Integer getMaximumRoundsForCourse();
    String getResultsMailRecipient();
}
