package com.sap.sailing.domain.swisstimingadapter;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import com.sap.sse.common.Distance;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public interface SailMasterListener {
    void receivedRacePositionData(String raceID, RaceStatus raceStatus, RacingStatus racingStatus, TimePoint timePoint,
            TimePoint startTime, Long millisecondsSinceRaceStart, Integer nextMarkIndexForLeader,
            Distance distanceToNextMarkForLeader, Collection<Fix> fixes);
    
    void receivedTimingData(String raceID, String boatID, List<Util.Triple<Integer, Integer, Long>> markIndicesRanksAndTimesSinceStartInMilliseconds);
    
    void receivedClockAtMark(String raceID, List<Util.Triple<Integer, TimePoint, String>> markIndicesTimePointsAndBoatIDs);
    
    void receivedStartList(String raceID, StartList startList) throws URISyntaxException;
    
    void receivedCourseConfiguration(String raceID, Course course) throws URISyntaxException;
    
    void receivedAvailableRaces(Iterable<Race> races);
    
    /**
     * Announces the progress of data loaded from a local persistent store (as opposed to receiving it live from a
     * SailMaster instance). 0.0 means no progress so far, 1.0 means loading from persistent store completed.
     */
    void storedDataProgress(String raceID, double progress);

    void receivedWindData(String raceID, int zeroBasedMarkIndex, double windDirectionTrueDegrees, double windSpeedInKnots);
}
