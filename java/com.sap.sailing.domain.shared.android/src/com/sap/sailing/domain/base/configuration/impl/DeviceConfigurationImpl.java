package com.sap.sailing.domain.base.configuration.impl;
import java.util.List;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;

public class DeviceConfigurationImpl implements DeviceConfiguration {

    private static final long serialVersionUID = 6084215932610324314L;
    
    private final RacingProceduresConfiguration proceduresConfiguration;
    
    private List<String> allowedCourseAreaNames;
    private Integer minRounds;
    private Integer maxRounds;
    private String resultsMailRecipient;
    
    public DeviceConfigurationImpl() {
        this.proceduresConfiguration = null;
    }

    public DeviceConfigurationImpl(RacingProceduresConfiguration proceduresConfiguration) {
        this.proceduresConfiguration = proceduresConfiguration;
    }

    @Override
    public RacingProceduresConfiguration getRacingProceduresConfiguration() {
        return proceduresConfiguration;
    }

    @Override
    public List<String> getAllowedCourseAreaNames() {
        return allowedCourseAreaNames;
    }

    public void setAllowedCourseAreaNames(List<String> newAllowedCourseAreaNames) {
        this.allowedCourseAreaNames = newAllowedCourseAreaNames;
    }

    @Override
    public Integer getMinimumRoundsForCourse() {
        return minRounds;
    }

    public void setMinimumRoundsForCourse(Integer minRounds) {
        this.minRounds = minRounds;
    }

    @Override
    public Integer getMaximumRoundsForCourse() {
        return maxRounds;
    }

    public void setMaximumRoundsForCourse(Integer maxRounds) {
        this.maxRounds = maxRounds;
    }

    @Override
    public String getResultsMailRecipient() {
        return resultsMailRecipient;
    }

    public void setResultsMailRecipient(String resultsMailRecipient) {
        this.resultsMailRecipient = resultsMailRecipient;
    }

}
