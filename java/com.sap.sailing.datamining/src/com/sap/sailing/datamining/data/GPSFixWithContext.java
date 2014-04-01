package com.sap.sailing.datamining.data;

import com.sap.sailing.datamining.WindStrengthCluster;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sse.datamining.shared.annotations.Dimension;

public interface GPSFixWithContext extends GPSFixMoving {
    
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
    
    /**
     * The leg type of the leg, which contains this gps fix. Can be <code>null</code> if there's no wind in the race.
     */
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

}
