package com.sap.sailing.domain.racelog;

import java.util.Calendar;
import java.util.Date;

import com.sap.sse.common.TimePoint;

public class RaceStateOfSameDayHelper {
    
    public static boolean isRaceStateOfSameDay(TimePoint raceStartTimePoint, TimePoint raceFinishedTimePoint, 
            TimePoint abortingTimePoint, Calendar now) {
        Date raceStartTime = null;
        Date raceFinishedTime = null;
        Date abortingTime = null;
        
        if (raceStartTimePoint != null) {
            raceStartTime = raceStartTimePoint.asDate();
        }
        if (raceFinishedTimePoint != null) {
            raceFinishedTime = raceFinishedTimePoint.asDate();
        }
        if (abortingTimePoint != null) {
            abortingTime = abortingTimePoint.asDate();
        }
        
        return isRaceStateOfSameDay(raceStartTime, raceFinishedTime, abortingTime, now);
    }
    
    public static boolean isRaceStateOfSameDay(Date raceStartTime, Date raceFinishedTime, Calendar now) {
        boolean result = false;
        if (raceFinishedTime != null) {
            Calendar finishedTimeCal = Calendar.getInstance();
            finishedTimeCal.setTime(raceFinishedTime);
            if(isSameDay(now, finishedTimeCal)) {
                result = true;
            }
        } else if (raceStartTime != null) {
            Calendar startTimeCal = Calendar.getInstance();
            startTimeCal.setTime(raceStartTime);
            if(isSameDay(now, startTimeCal)) {
                result = true;
            }
        }
        
        return result;
    }
    
    public static boolean isRaceStateOfSameDay(Date raceStartTime, Date raceFinishedTime, Date abortingTime, Calendar now) {
        boolean result = isRaceStateOfSameDay(raceStartTime, raceFinishedTime, now);
        if (!result && (abortingTime != null)) {
            Calendar abortingTimeCal = Calendar.getInstance();
            abortingTimeCal.setTime(abortingTime);
            if(isSameDay(now, abortingTimeCal)) {
                result = true;
            }
        }
        return result;
    }
    
    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

}
