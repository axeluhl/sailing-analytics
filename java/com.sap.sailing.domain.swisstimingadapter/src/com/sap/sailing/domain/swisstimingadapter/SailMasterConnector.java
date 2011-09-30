package com.sap.sailing.domain.swisstimingadapter;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.util.Util.Pair;

public interface SailMasterConnector {
    SailMasterMessage sendRequestAndGetResponse(String requestMessage) throws UnknownHostException, IOException;
    
    Iterable<Race> getRaces() throws UnknownHostException, IOException;
    
    Course getCourse(String raceID) throws UnknownHostException, IOException;
    
    StartList getStartList(String raceID) throws UnknownHostException, IOException;
    
    TimePoint getStartTime(String raceID) throws UnknownHostException, IOException;
    
    /**
     * @param markIndex
     *            use -1 to obtain timings for all marks
     * @return a map with the single mark index requested by <code>markIndex</code> or all mark indices (in case of
     *         <code>markIndex==-1</code>) as key; pairs of gap (TODO in milliseconds?) and boat ID as values.
     */
    Map<Integer, Pair<Long, String>> getDeltaClockAtMark(String raceID, int markIndex) throws UnknownHostException, IOException;
    
    double getDistanceToMarkInMeters(String raceID, int markIndex, String boatID) throws UnknownHostException, IOException;
    
    double getCurrentBoatSpeedInMetersPerSecond(String raceID, String boatID) throws UnknownHostException, IOException;
    
    double getAverageBoatSpeedInMetersPerSecond(String raceID, String leg, String boatID) throws UnknownHostException, IOException;
    
    double getDistanceBetweenBoatsInMeters(String raceID, String boatID1, String boatID2) throws UnknownHostException, IOException;
}
