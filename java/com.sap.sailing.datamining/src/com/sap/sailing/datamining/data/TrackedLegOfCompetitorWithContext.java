package com.sap.sailing.datamining.data;

import com.sap.sailing.datamining.WindStrengthCluster;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LegType;

public interface TrackedLegOfCompetitorWithContext {
    
    public String getRegattaName();
    public String getRaceName();
    public int getLegNumber();
    public String getCourseAreaName();
    public String getFleetName();
    public String getBoatClassName();
    /**
     * The year of the start of the race.
     */
    public Integer getYear();
    public LegType getLegType();
    public String getCompetitorName();
    public String getCompetitorSailID();
    public String getCompetitorNationality();
    public WindStrengthCluster getWindStrength();
    public Distance getDistanceTraveled();

}
