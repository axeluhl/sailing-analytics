package com.sap.sailing.domain.swisstimingadapter;

import java.util.Collection;
import java.util.List;

import com.sap.sse.common.Distance;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public abstract class SailMasterAdapter implements SailMasterListener {

    @Override
    public void receivedRacePositionData(String raceID, RaceStatus raceStatus, RacingStatus racingStatus, TimePoint timePoint,
            TimePoint startTime, Long millisecondsSinceRaceStart, Integer nextMarkIndexForLeader,
            Distance distanceToNextMarkForLeader, Collection<Fix> fixes) {
    }

    @Override
    public void receivedTimingData(String raceID, String boatID,
            List<Util.Triple<Integer, Integer, Long>> markIndicesRanksAndTimesSinceStartInMilliseconds) {
    }

    @Override
    public void receivedClockAtMark(String raceID,
            List<Util.Triple<Integer, TimePoint, String>> markIndicesTimePointsAndBoatIDs) {
    }

    @Override
    public void receivedStartList(String raceID, StartList startList) {
    }

    @Override
    public void receivedCourseConfiguration(String raceID, Course course) {
    }

    @Override
    public void receivedAvailableRaces(Iterable<Race> races) {
    }

    @Override
    public void storedDataProgress(String raceID, double progress) {
    }

    @Override
    public void receivedWindData(String raceID, int markIndex, double windDirectionTrueDegrees, double windSpeedInKnots) {
    }

}
