package com.sap.sailing.domain.base.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.TabletConfiguration;

public class TabletConfigurationImpl implements TabletConfiguration {
    
    private Set<String> allowedCourseAreaNames;
    private Integer minRounds;
    private Integer maxRounds;
    private String resultsMailRecipent;
    
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
    public String getResultsMailRecipent() {
        return resultsMailRecipent;
    }

    public void setResultsMailRecipent(String ResultsMailRecipent) {
        this.resultsMailRecipent = ResultsMailRecipent;
    }

}
