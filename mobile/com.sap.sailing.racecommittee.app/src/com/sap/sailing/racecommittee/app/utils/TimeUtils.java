package com.sap.sailing.racecommittee.app.utils;

import android.text.format.DateFormat;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.util.Calendar;

public class TimeUtils {

    private TimeUtils() {
        // only static methods
    }

    public static long timeUntil(TimePoint targetTime) {
        return targetTime.asMillis() - MillisecondsTimePoint.now().asMillis();
    }
    
    /**
     * Formats your time to 'kk:mm:ss'.
     * @param timePoint timestamp to format
     */
    public static String formatTime(TimePoint timePoint) {
        return formatTime(timePoint, "kk:mm:ss");
    }
    
    /**
     * Formats your time with the help of {@link DateFormat}.
     * @param timePoint timestamp to format
     * @param format format as defined by {@link DateFormat}
     * @return timestamp formatted as {@link String}
     */
    public static String formatTime(TimePoint timePoint, String format) {
        return DateFormat.format(format, timePoint.asDate()).toString();
    }

    public static String formatDurationSince(long milliseconds) {
        int secondsTillStart = (int) Math.floor(milliseconds / 1000f);
        return formatDuration(secondsTillStart);
    }
    
    public static String formatDurationUntil(long milliseconds) {
        int secondsTillStart = (int) Math.ceil(milliseconds / 1000f);
        return formatDuration(secondsTillStart);
    }

    public static int daysBetween(Calendar day1, Calendar day2){
        Calendar dayOne = (Calendar) day1.clone();
        Calendar dayTwo = (Calendar) day2.clone();

        if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
            return Math.abs(dayOne.get(Calendar.DAY_OF_YEAR) - dayTwo.get(Calendar.DAY_OF_YEAR));
        } else {
            if (dayTwo.get(Calendar.YEAR) > dayOne.get(Calendar.YEAR)) {
                //swap them
                Calendar temp = dayOne;
                dayOne = dayTwo;
                dayTwo = temp;
            }
            int extraDays = 0;

            while (dayOne.get(Calendar.YEAR) > dayTwo.get(Calendar.YEAR)) {
                dayOne.add(Calendar.YEAR, -1);
                // getActualMaximum() important for leap years
                extraDays += dayOne.getActualMaximum(Calendar.DAY_OF_YEAR);
            }

            return extraDays - dayTwo.get(Calendar.DAY_OF_YEAR) + dayOne.get(Calendar.DAY_OF_YEAR);
        }
    }

    private static String formatDuration(int secondsTillStart) {
        int hours = secondsTillStart / 3600;
        int minutes = (secondsTillStart % 3600) / 60;
        int seconds = (secondsTillStart % 60);
        String timePattern = "%s:%s:%s";
        String secondsString = seconds < 10 ? "0" + seconds : "" + seconds;
        String minutesString = minutes < 10 ? "0" + minutes : "" + minutes;
        String hoursString = hours < 10 ? "0" + hours : "" + hours;
        return String.format(timePattern, hoursString, minutesString, secondsString);
    }
}
