package com.sap.sailing.domain.base.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.TabletConfiguration;

public class TabletConfigurationImpl implements TabletConfiguration {
    
    private Set<String> allowedCourseAreaNames;
    
    public TabletConfigurationImpl() {
        this.allowedCourseAreaNames = new HashSet<String>();
    }

    @Override
    public Set<String> getAllowedCourseAreaNames() {
        return allowedCourseAreaNames;
    }

    public void setAllowedCourseAreaNames(Set<String> newAllowedCourseAreaNames) {
        this.allowedCourseAreaNames = newAllowedCourseAreaNames;
    }

}
