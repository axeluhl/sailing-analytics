package com.sap.sailing.domain.swisstimingadapter;

import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.util.Util.Pair;
import com.sap.sailing.util.Util.Triple;

public interface SailMasterListener {
    void receivedRacePositionData(String raceID, RaceStatus status, TimePoint timePoint, TimePoint startTime,
            Long millisecondsSinceRaceStart, Integer nextMarkIndexForLeader, Distance distanceToNextMarkForLeader,
            Collection<Fix> fixes);
    
    void receivedTimingData(String raceID, String boatID, List<Triple<Integer, Integer, Long>> markIndicesRanksAndTimesSinceStartInMilliseconds);
    
    void receivedClockAtMark(String raceID, List<Triple<Integer, TimePoint, String>> markIndicesTimePointsAndBoatIDs);
    
    void receivedStartList(String raceID, List<Triple<String, String, String>> boatIDsISOCountryCodesAndNames);
    
    void receivedCourseConfiguration(String raceID, List<Mark> marks);
    
    void receivedAvailableRaces(List<Pair<String, String>> raceIDsAndDescriptions);
}
