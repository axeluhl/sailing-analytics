package com.sap.sailing.datamining.data;

import com.sap.sailing.datamining.WindStrengthCluster;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LegType;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.SideEffectFreeValue;

public interface TrackedLegOfCompetitorWithContext {
    
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
    
    @SideEffectFreeValue("distanceTraveled")
    public Distance getDistanceTraveled();

}
