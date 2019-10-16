package com.sap.sailing.domain.common;

import java.util.Date;

import com.sap.sse.common.Util;

public class RaceTimesCalculationUtil {
    public static final long MIN_TIME_BEFORE_RACE_START = 3 * 60 * 1000; // 3 minutes
    public static final long MIN_TIME_AFTER_RACE_START = 5 * 60 * 1000; // 5 minutes
    public static final long TIME_AFTER_LIVE = 5 * 60 * 1000; // 5 minutes
    public static final long MAX_TIME_AFTER_RACE_END = 3 * 60 * 1000; // 3 minutes

    public static Util.Pair<Date, Date> calculateRaceMinMax(Date liveTimePoint, Date startOfTracking, Date startOfRace,
            Date raceFinishingTime, Date raceFinishedTime, Date endOfRace, Date endOfTracking) {
        if (liveTimePoint == null) {
            return new Util.Pair<Date, Date>(null, null);
        }
        Date min = null;
        Date max = null;

        // Range start / min
        if (startOfRace != null) {
            // We have a start time
            min = new Date(startOfRace.getTime() - MIN_TIME_BEFORE_RACE_START);
        } else if (startOfTracking != null) {
            // We at least have a start of tracking time
            min = startOfTracking;
        }
        if (min == null || liveTimePoint.before(min)){
            // There is no start time or the start time is in the future
            min = new Date(liveTimePoint.getTime());
        }

        // Range end / max
        if (raceFinishedTime != null) {
            // We have a blue flag down event
            max = new Date(raceFinishedTime.getTime() + MAX_TIME_AFTER_RACE_END);
        } else if (endOfRace != null && raceFinishingTime == null) {
            // We have NO blue flag up event and we have an end of race time
            max = new Date(endOfRace.getTime() + MAX_TIME_AFTER_RACE_END);
        } else {
            // We have no end time so just keep playing until they are created
            max = new Date(liveTimePoint.getTime() + TIME_AFTER_LIVE);
        }
        // If there are no end events or the additional offset exceeds end of tracking
        if (endOfTracking != null) {
            if (max == null || max.after(endOfTracking)) {
                max = endOfTracking;
            }
        }

        return new Util.Pair<Date, Date>(min, max);
    }
}
