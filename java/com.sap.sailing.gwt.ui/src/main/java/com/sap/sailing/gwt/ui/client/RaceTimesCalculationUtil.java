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
        Date min = null;
        Date max = null;

        Date liveTimePoint = timer.getLiveTimePointAsDate();
        switch (timer.getPlayMode()) {
        case Live:
            if (raceTimesInfo.startOfRace != null) {
                // we have a race start time
                if (raceTimesInfo.startOfRace.after(liveTimePoint)) {
                    // race start is in the future
                    min = new Date(Math.min(liveTimePoint.getTime(), raceTimesInfo.startOfRace.getTime() - MIN_TIME_BEFORE_RACE_START));
                    max = new Date(raceTimesInfo.startOfRace.getTime() + MIN_TIME_AFTER_RACE_START);
                } else {
                    // race start was in the past
                    min = new Date(raceTimesInfo.startOfRace.getTime() - MIN_TIME_BEFORE_RACE_START);
                    max = new Date(liveTimePoint.getTime() + TIME_AFTER_LIVE);
                }
            } else {
                // we have NO race start time
                min = raceTimesInfo.startOfTracking;
                max = new Date(liveTimePoint.getTime() + TIME_AFTER_LIVE);
            }
            break;
        case Replay:
            if (raceTimesInfo.startOfRace != null) {
                min = new Date(raceTimesInfo.startOfRace.getTime() - MIN_TIME_BEFORE_RACE_START);
            } else if (raceTimesInfo.startOfTracking != null) {
                min = raceTimesInfo.startOfTracking;
            }
            
            // If there is a blue flag up event use the blue flag down event (may not exist yet)
            if (false) { //TODO is there a blue flag up event
                max = new Date(raceTimesInfo.raceFinishedTime.getTime() + MAX_TIME_AFTER_RACE_END);
            } else { // Else use the endOfRace
                max = new Date(raceTimesInfo.endOfRace.getTime() + MAX_TIME_AFTER_RACE_END);
            }
            
            // If there are no end events or the additional offset exceeds end of tracking
            if (raceTimesInfo.endOfTracking != null) {
                if (max == null || max.after(raceTimesInfo.endOfTracking)) {
                    max = raceTimesInfo.endOfTracking;
                }
            }
            break;
        }
        return new Util.Pair<Date, Date>(min, max);
    }
}
