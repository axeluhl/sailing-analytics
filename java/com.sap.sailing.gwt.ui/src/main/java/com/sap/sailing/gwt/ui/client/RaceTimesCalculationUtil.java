package com.sap.sailing.gwt.ui.client;

import java.util.Date;

import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.player.Timer;

public class RaceTimesCalculationUtil {

    public static Util.Pair<Date, Date> caluclateRaceMinMax(Timer timer, RaceTimesInfoDTO raceTimesInfo) {
        Date min = null;
        Date max = null;

        Date liveTimePoint = timer.getLiveTimePointAsDate();
        switch (timer.getPlayMode()) {
        case Live:
            if (raceTimesInfo.startOfRace != null) {
                // we have a race start time
                if (raceTimesInfo.startOfRace.after(liveTimePoint)) {
                    // race start is in the future
                    long extensionTime = calculateRaceExtensionTime(liveTimePoint, raceTimesInfo.startOfRace);
                    min = new Date(liveTimePoint.getTime() - extensionTime);
                    max = raceTimesInfo.startOfRace; // TODO Jonas: should we add some margin to this to avoid the start marker to be squeezed into the right border?
                } else {
                    // race start was in the past
                    long extensionTime = calculateRaceExtensionTime(raceTimesInfo.startOfRace, liveTimePoint);
                    min = new Date(raceTimesInfo.startOfRace.getTime() - extensionTime);
                    max = liveTimePoint; // TODO Jonas: what about the margin here?
                }
            } else {
                // we have NO race start time
                min = raceTimesInfo.startOfTracking;
                max = liveTimePoint; // TODO Jonas: what about the margin here?
            }
            break;
        case Replay:
            long extensionTime = calculateRaceExtensionTime(raceTimesInfo.startOfRace, raceTimesInfo.endOfRace);
            if (raceTimesInfo.startOfRace != null) {
                min = new Date(raceTimesInfo.startOfRace.getTime() - extensionTime);
            } else if (raceTimesInfo.startOfTracking != null) {
                min = raceTimesInfo.startOfTracking;
            }
            if (raceTimesInfo.endOfRace != null) {
                max = new Date(raceTimesInfo.endOfRace.getTime() + extensionTime);
            } else if (raceTimesInfo.newestTrackingEvent != null) {
                max = raceTimesInfo.newestTrackingEvent;
                if (raceTimesInfo.endOfTracking != null && raceTimesInfo.endOfTracking.before(raceTimesInfo.newestTrackingEvent)) {
                    max = raceTimesInfo.endOfTracking;
                }
            } else if (raceTimesInfo.endOfTracking != null) {
                max = raceTimesInfo.endOfTracking;
            }
            break;
        }
        return new Util.Pair<Date, Date>(min, max);
    }
    
    private static long calculateRaceExtensionTime(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) {
            return 5 * 60 * 1000; //5 minutes
        }
        
        long minExtensionTime = 60 * 1000; // 1 minute
        long maxExtensionTime = 10 * 60 * 1000; // 10 minutes
        double extensionTimeFactor = 0.1; // 10 percent of the interval length
        long extensionTime = (long) ((endTime.getTime() - startTime.getTime()) * extensionTimeFactor);
        
        return extensionTime < minExtensionTime ? minExtensionTime : extensionTime > maxExtensionTime ? maxExtensionTime : extensionTime;
    }

}
