package com.sap.sailing.racecommittee.app.utils;

import android.content.Context;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.util.ArrayList;
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
     *
     * @param timePoint timestamp to format
     */
    public static String formatTime(TimePoint timePoint) {
        return formatTime(timePoint, "kk:mm:ss");
    }

    /**
     * Formats your time with the help of {@link DateFormat}.
     *
     * @param timePoint timestamp to format
     * @param format    format as defined by {@link DateFormat}
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

    public static String calcDuration(Calendar from, Calendar to) {
        String retValue;

        long millis = to.getTimeInMillis() - from.getTimeInMillis();

        long min = millis / (1000 * 60);
        long sec = (millis - (min * 60 * 1000)) / 1000;

        retValue = String.valueOf(sec) + "\"";
        if (retValue.length() == 2) {
            retValue = "0" + retValue;
        }
        if (min > 0) {
            retValue = String.valueOf(min) + "' " + retValue;
        }

        return retValue;
    }

    public static Calendar floorTime(Calendar calendar) {
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }

    public static int daysBetween(Calendar day1, Calendar day2) {
        Calendar dayOne = (Calendar) day1.clone();
        Calendar dayTwo = (Calendar) day2.clone();

        if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
            return dayOne.get(Calendar.DAY_OF_YEAR) - dayTwo.get(Calendar.DAY_OF_YEAR);
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
        boolean negative = (hours < 0 || minutes < 0 || seconds < 0);
        String timePattern = ((negative) ? "-" : "") + "%s:%s:%s";
        String secondsString = seconds < 10 ? "0" + Math.abs(seconds) : "" + Math.abs(seconds);
        String minutesString = minutes < 10 ? "0" + Math.abs(minutes) : "" + Math.abs(minutes);
        String hoursString = hours < 10 ? "0" + Math.abs(hours) : "" + Math.abs(hours);
        return String.format(timePattern, hoursString, minutesString, secondsString);
    }

    public static void initDatePicker(Context context, NumberPicker datePicker, Calendar time, int pastDays, int futureDays) {
        initDatePicker(context, datePicker, time, pastDays, futureDays, true);
    }

    public static void initDatePicker(Context context, NumberPicker datePicker, Calendar time, int pastDays, int futureDays, boolean useWords) {
        java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance();
        ArrayList<String> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time.getTime());
        // Past
        calendar.add(Calendar.DAY_OF_MONTH, pastDays);
        for (int i = pastDays; i < -1; i++) {
            dates.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        if (useWords) {
            dates.add(context.getString(R.string.yesterday));
            dates.add(context.getString(R.string.today));
            dates.add(context.getString(R.string.tomorrow));
            calendar.add(Calendar.DAY_OF_MONTH, 3);
        } else {
            for (int i = 0; i < 3; i++) {
                dates.add(dateFormat.format(calendar.getTime()));
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        // Future
        for (int i = 3; i <= futureDays + 1; i++) {
            dates.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        datePicker.setDisplayedValues(dates.toArray(new String[dates.size()]));
        datePicker.setMinValue(0);
        datePicker.setMaxValue(dates.size() - 1);
        datePicker.setWrapSelectorWheel(false);
        datePicker.setValue(TimeUtils.daysBetween(calendar, Calendar.getInstance()) + Math.abs(pastDays));
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            TextView date = new TextView(context);
            date.setText(dateFormat.format(calendar.getTime()));
            int padding = context.getResources().getDimensionPixelSize(R.dimen.default_padding_half);
            date.setPadding(padding, padding, padding, padding);
            date.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            datePicker.getLayoutParams().width = date.getMeasuredWidth();
        }
    }
}
