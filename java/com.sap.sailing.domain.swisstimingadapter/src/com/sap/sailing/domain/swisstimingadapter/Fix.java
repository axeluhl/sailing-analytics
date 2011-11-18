package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.SpeedWithBearing;

/**
 * Data fix transmitted periodically from the SwissTiming Sail Master system, telling boat position and speed
 * data.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface Fix {
    String getBoatID();
    
    TrackerType getTrackerType();
    
    Long getAgeOfDataInMilliseconds();
    
    Position getPosition();
    
    SpeedWithBearing getSpeed();
    
    Integer getNextMarkIndex();
    
    Integer getRank();
    
    Speed getAverageSpeedOverGroundPerLeg();
    
    Speed getVelocityMadeGood();
    
    Distance getDistanceToLeader();
    
    Distance getDistanceToNextMark();
}
