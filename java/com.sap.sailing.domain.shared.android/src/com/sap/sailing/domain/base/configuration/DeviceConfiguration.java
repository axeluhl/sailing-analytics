package com.sap.sailing.domain.base.configuration;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;

/**
 * Interface holding information about tablet's (RCApp) configuration.
 */
public interface DeviceConfiguration extends Serializable {
    // General Configuration
    List<String> getAllowedCourseAreaNames();
    String getResultsMailRecipient();
    
    // Overwrites for all races
    RacingProcedureType getDefaultRacingProcedureType();
    CourseDesignerMode getDefaultCourseDesignerMode();
    
    // Course Designer Configurations
    List<String> getByNameCourseDesignerCourseNames();
    
    // Racing Procedure Configurations
    RacingProceduresConfiguration getRacingProceduresConfiguration();
}
