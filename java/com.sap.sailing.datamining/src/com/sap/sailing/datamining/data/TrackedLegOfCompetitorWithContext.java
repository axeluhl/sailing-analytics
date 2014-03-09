package com.sap.sailing.datamining.data;

import com.sap.sailing.datamining.WindStrengthCluster;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LegType;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.SideEffectFreeValue;
import com.sap.sse.datamining.shared.Unit;

public interface TrackedLegOfCompetitorWithContext {
    
    @Dimension(messageKey="regatta")
    public String getRegattaName();
    
    @Dimension(messageKey="race")
    public String getRaceName();
    
    @Dimension(messageKey="legNumber")
    public int getLegNumber();
    
    @Dimension(messageKey="course")
    public String getCourseAreaName();
    
    @Dimension(messageKey="fleet")
    public String getFleetName();
    
    @Dimension(messageKey="boatClass")
    public String getBoatClassName();
    
    /**
     * The year of the start of the race.
     */
    @Dimension(messageKey="year")
    public Integer getYear();
    
    @Dimension(messageKey="legType")
    public LegType getLegType();
    
    @Dimension(messageKey="competitor")
    public String getCompetitorName();
    
    @Dimension(messageKey="sailID")
    public String getCompetitorSailID();
    
    @Dimension(messageKey="nationality")
    public String getCompetitorNationality();
    
    @Dimension(messageKey="windStrength")
    public WindStrengthCluster getWindStrength();
    
    @SideEffectFreeValue(messageKey="distanceTraveled", resultUnit=Unit.Meters)
    public Distance getDistanceTraveled();

}
