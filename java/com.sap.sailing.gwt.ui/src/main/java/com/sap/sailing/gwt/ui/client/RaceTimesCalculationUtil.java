package com.sap.sailing.gwt.ui.client;

import java.util.Date;

import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.player.Timer;

public class RaceTimesCalculationUtil {
    public static final long MIN_TIME_BEFORE_RACE_START = 3 * 60 * 1000; // 3 minutes
    public static final long MIN_TIME_AFTER_RACE_START = 5 * 60 * 1000; // 5 minutes
    public static final long TIME_AFTER_LIVE = 5 * 60 * 1000; // 5 minutes
    public static final long MAX_TIME_AFTER_RACE_END = 1 * 60 * 1000; // 1 minutes

    public static Util.Pair<Date, Date> calculateRaceMinMax(Timer timer, RaceTimesInfoDTO raceTimesInfo) {
        if (timer == null || raceTimesInfo == null) {
            return new Util.Pair<Date, Date>(null, null);
        }
        Date min = null;
        Date max = null;

        Date liveTimePoint = timer.getLiveTimePointAsDate();

        // Range start / min
        if (raceTimesInfo.startOfRace != null) {
            // We have a start time
            min = new Date(raceTimesInfo.startOfRace.getTime() - MIN_TIME_BEFORE_RACE_START);
        } else if (raceTimesInfo.startOfTracking != null) {
            // We at least have a start of tracking time
            min = raceTimesInfo.startOfTracking;
        }
        if (min == null || liveTimePoint.before(min)){
            // There is no start time or the start time is in the future
            min = new Date(liveTimePoint.getTime());
        }

        // Range end / max
        if (raceTimesInfo.raceFinishedTime != null) {
            // We have a blue flag down event
            max = new Date(raceTimesInfo.raceFinishedTime.getTime() + MAX_TIME_AFTER_RACE_END);
        } else if (raceTimesInfo.endOfRace != null && raceTimesInfo.raceFinishingTime == null) {
            // We have NO blue flag up event and we have an end of race time
            max = new Date(raceTimesInfo.endOfRace.getTime() + MAX_TIME_AFTER_RACE_END);
        } else {
            // We have no end time so just keep playing until they are created
            max = new Date(liveTimePoint.getTime() + TIME_AFTER_LIVE);
        }
        // If there are no end events or the additional offset exceeds end of tracking
        if (raceTimesInfo.endOfTracking != null) {
            if (max == null || max.after(raceTimesInfo.endOfTracking)) {
                max = raceTimesInfo.endOfTracking;
            }
        }

        return new Util.Pair<Date, Date>(min, max);
    }
}
