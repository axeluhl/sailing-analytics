package com.sap.sailing.domain.base.configuration;

import java.io.Serializable;
import java.util.List;

/**
 * Interface holding information about tablet's (RCApp) configuration.
 */
public interface DeviceConfiguration extends Serializable {
    List<String> getAllowedCourseAreaNames();
    Integer getMinimumRoundsForCourse();
    Integer getMaximumRoundsForCourse();
    String getResultsMailRecipient();
}
