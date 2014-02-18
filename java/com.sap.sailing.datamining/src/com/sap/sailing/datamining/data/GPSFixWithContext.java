package com.sap.sailing.datamining.data;

import com.sap.sailing.datamining.WindStrengthCluster;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sse.datamining.annotations.Dimension;

public interface GPSFixWithContext extends GPSFixMoving {
    
    @Dimension("regatta")
    public String getRegattaName();
    
    @Dimension("race")
    public String getRaceName();
    
    @Dimension("legNumber")
    public int getLegNumber();
    
    @Dimension("course")
    public String getCourseAreaName();
    
    @Dimension("fleet")
    public String getFleetName();
    
    @Dimension("boatClass")
    public String getBoatClassName();
    
    /**
     * The year of the start of the race.
     */
    @Dimension("year")
    public Integer getYear();
    
    /**
     * The leg type of the leg, which contains this gps fix. Can be <code>null</code> if there's no wind in the race.
     */
    @Dimension("legType")
    public LegType getLegType();
    
    @Dimension("competitor")
    public String getCompetitorName();
    
    @Dimension("sailID")
    public String getCompetitorSailID();
    
    @Dimension("nationality")
    public String getCompetitorNationality();
    
    @Dimension("windStrength")
    public WindStrengthCluster getWindStrength();

}
