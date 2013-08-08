package com.sap.sailing.datamining;

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
    
    
//    /**
//     * The wind strength at the position and the time of this gps fix. Can be <code>null</code> if there's no wind in the race.
//     */
//    /**
//     * A string representation of the value of the given dimension of this GPS-Fix (for example the name of
//     * the competitor of this fix, if the dimension is {@link Dimension#CompetitorName}). Can be <code>null</code>,
//     * if the value is <code>null</code> (possible for wind or leg type).
//     * 
//     * @return A string representation of the value of the given dimension of this GPS-Fix.
//     */

}
