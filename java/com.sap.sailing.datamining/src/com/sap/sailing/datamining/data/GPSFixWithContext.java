package com.sap.sailing.datamining.data;

import com.sap.sailing.datamining.WindStrengthCluster;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public interface GPSFixWithContext extends GPSFixMoving {
    
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
    /**
     * The leg type of the leg, which contains this gps fix. Can be <code>null</code> if there's no wind in the race.
     */
    public LegType getLegType();
    public String getCompetitorName();
    public String getCompetitorSailID();
    public String getCompetitorNationality();
    public WindStrengthCluster getWindStrength();

}
