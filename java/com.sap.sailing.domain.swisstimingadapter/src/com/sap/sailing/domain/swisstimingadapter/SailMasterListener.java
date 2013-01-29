package com.sap.sailing.domain.swisstimingadapter;

import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;

public interface SailMasterListener {
    void receivedRacePositionData(String raceID, RaceStatus status, TimePoint timePoint, TimePoint startTime,
            Long millisecondsSinceRaceStart, Integer nextMarkIndexForLeader, Distance distanceToNextMarkForLeader,
            Collection<Fix> fixes);
    
    void receivedTimingData(String raceID, String boatID, List<Triple<Integer, Integer, Long>> markIndicesRanksAndTimesSinceStartInMilliseconds);
    
    void receivedClockAtMark(String raceID, List<Triple<Integer, TimePoint, String>> markIndicesTimePointsAndBoatIDs);
    
    void receivedStartList(String raceID, StartList startList);
    
    void receivedCourseConfiguration(String raceID, Course course);
    
    void receivedAvailableRaces(Iterable<Race> races);
}
