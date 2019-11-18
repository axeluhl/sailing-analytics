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
        return calculateRaceMinMax(liveTimePoint, startOfTracking, startOfRace, raceFinishingTime, raceFinishedTime,
                endOfRace, endOfTracking, MIN_TIME_BEFORE_RACE_START, MAX_TIME_AFTER_RACE_END, TIME_AFTER_LIVE);
    }

    public static Util.Pair<Date, Date> calculateRaceMinMax(Date liveTimePoint, Date startOfTracking, Date startOfRace,
            Date raceFinishingTime, Date raceFinishedTime, Date endOfRace, Date endOfTracking, long millisBeforeStart,
            long millisAfterEnd, long millisAfterLive) {
        if (liveTimePoint == null) {
            return new Util.Pair<Date, Date>(null, null);
        }
        Date min = null;
        Date max = null;

        // Range start / min
        if (startOfRace != null) {
            // We have a start time
            min = new Date(startOfRace.getTime() - millisBeforeStart);
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
            max = new Date(raceFinishedTime.getTime() + millisAfterEnd);
        } else if (endOfRace != null && raceFinishingTime == null) {
            // We have NO blue flag up event and we have an end of race time
            max = new Date(endOfRace.getTime() + millisAfterEnd);
        } else {
            // We have no end time and are not expecting any end events
            if (endOfTracking != null && raceFinishingTime == null) {
                max = endOfTracking;
            } else {
                // No end in sight; Keep on playing
                max = new Date(liveTimePoint.getTime() + millisAfterLive);
            }
        }

        return new Util.Pair<Date, Date>(min, max);
    }
}
