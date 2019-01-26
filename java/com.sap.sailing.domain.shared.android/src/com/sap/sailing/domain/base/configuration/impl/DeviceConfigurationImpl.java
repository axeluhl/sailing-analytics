package com.sap.sailing.domain.base.configuration.impl;

import java.util.List;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;

public class DeviceConfigurationImpl implements DeviceConfiguration {

    private static final long serialVersionUID = 6084215932610324314L;
    
    private RegattaConfiguration regattaConfiguration;
    
    private List<String> allowedCourseAreaNames;
    private String resultsMailRecipient;
    private List<String> byNameDesignerCourseNames;

    public DeviceConfigurationImpl(RegattaConfiguration regattaConfiguration) {
        this.regattaConfiguration = regattaConfiguration;
    }
    
    public void setRegattaConfiguration(RegattaConfiguration proceduresConfiguration) {
        this.regattaConfiguration = proceduresConfiguration;
    }

    @Override
    public RegattaConfiguration getRegattaConfiguration() {
        return regattaConfiguration;
    }

    @Override
    public List<String> getAllowedCourseAreaNames() {
        return allowedCourseAreaNames;
    }

    public void setAllowedCourseAreaNames(List<String> newAllowedCourseAreaNames) {
        this.allowedCourseAreaNames = newAllowedCourseAreaNames;
    }

    @Override
    public String getResultsMailRecipient() {
        return resultsMailRecipient;
    }

    public void setResultsMailRecipient(String resultsMailRecipient) {
        this.resultsMailRecipient = resultsMailRecipient;
    }
    
    @Override
    public List<String> getByNameCourseDesignerCourseNames() {
        return byNameDesignerCourseNames;
    }

    public void setByNameDesignerCourseNames(List<String> byNameDesignerCourseNames) {
        this.byNameDesignerCourseNames = byNameDesignerCourseNames;
    }
    
    public DeviceConfiguration copy() {
        DeviceConfigurationImpl copyConfiguration = new DeviceConfigurationImpl(regattaConfiguration.clone());
        copyConfiguration.setAllowedCourseAreaNames(allowedCourseAreaNames);
        copyConfiguration.setByNameDesignerCourseNames(byNameDesignerCourseNames);
        copyConfiguration.setResultsMailRecipient(resultsMailRecipient);
        return copyConfiguration;
    }

}
