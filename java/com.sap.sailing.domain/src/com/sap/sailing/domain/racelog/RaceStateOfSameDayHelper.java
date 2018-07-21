package com.sap.sailing.domain.racelog;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class RaceStateOfSameDayHelper {
    
    public static boolean isRaceStateOfSameDay(TimePoint raceStartTimePoint, TimePoint raceFinishedTimePoint, 
            TimePoint abortingTimePoint, Calendar now, Duration clientTimeZoneOffset) {
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
        return isRaceStateOfSameDay(raceStartTime, raceFinishedTime, abortingTime, now, clientTimeZoneOffset);
    }
    
    public static boolean isRaceStateOfSameDay(Date raceStartTime, Date raceFinishedTime, Calendar now, Duration clientTimeZoneOffset) {
        final boolean result;
        final Calendar timeToCheck = Calendar.getInstance();
        // see if we shall and can adjust the time zone offset for determining the day boundaries:
        if (clientTimeZoneOffset != null) {
            final String[] availableTimeZoneIDsForOffset = TimeZone.getAvailableIDs((int) -clientTimeZoneOffset.asMillis());
            if (availableTimeZoneIDsForOffset != null && availableTimeZoneIDsForOffset.length > 0) {
                // found a time zone with the offset requested; note that this may not be the time zone the client really is in
                // but at least for the current time (which may be different from the "now" parameter) that time zone has
                // the clientTimeZoneOffset from UTC.
                final TimeZone tz = TimeZone.getTimeZone(availableTimeZoneIDsForOffset[0]);
                timeToCheck.setTimeZone(tz);
                now.setTimeZone(tz);
            }
        }
        if (raceFinishedTime != null || raceStartTime != null) {
            if (raceFinishedTime != null) {
                timeToCheck.setTime(raceFinishedTime);
            } else if (raceStartTime != null) {
                timeToCheck.setTime(raceStartTime);
            }
            result = isSameDay(now, timeToCheck);
        } else {
            result = false;
        }
        return result;
    }
    
    /**
     * The client's day starts at <code>00:00:00Z - clientTimeZoneOffset</code> and ends at <code>23:59:59Z - clientTimeZoneOffset</code>.
     */
    public static boolean isRaceStateOfSameDay(Date raceStartTime, Date raceFinishedTime, Date abortingTime, Calendar now, Duration clientTimeZoneOffset) {
        boolean result = isRaceStateOfSameDay(raceStartTime, raceFinishedTime, now, clientTimeZoneOffset);
        if (!result && (abortingTime != null)) {
            Calendar abortingTimeCal = Calendar.getInstance();
            abortingTimeCal.setTime(abortingTime);
            if (isSameDay(now, abortingTimeCal)) {
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
