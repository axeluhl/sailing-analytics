package com.sap.sailing.gwt.common.client;

import java.util.Date;

import com.google.gwt.i18n.shared.DateTimeFormat;

/**
 * Some date calculation functions as GWT does not support java.util.Calendar
 * @author Frank
 *
 */
public class DateUtil {
    private static final DateTimeFormat dayFormat = DateTimeFormat.getFormat("d");
    private static final DateTimeFormat monthFormat = DateTimeFormat.getFormat("M");
    private static final DateTimeFormat yearFormat = DateTimeFormat.getFormat("yyyy");
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24; 
    
    public static int getDayOfMonth(Date date) {
        String s = dayFormat.format(date);
        return Integer.parseInt(s); 
    }

    public static int getMonth(Date date) {
        String s = monthFormat.format(date);
        return Integer.parseInt(s); 
    }

    public static int getYear(Date date) {
        String s = yearFormat.format(date);
        return Integer.parseInt(s); 
    }

    public static boolean isSameDayOfMonth(Date date1, Date date2) {
        return isSameMonth(date1, date2) && getDayOfMonth(date1) == getDayOfMonth(date2);
    }

    public static boolean isSameMonth(Date date1, Date date2) {
        return isSameYear(date1, date2) && getMonth(date1) == getMonth(date2);
    }

    public static boolean isSameYear(Date date1, Date date2) {
        return getYear(date1) == getYear(date2);
    }

    public static boolean isToday(Date date) {
        Date now = new Date();
        long diffInMillis = date.getTime() - now.getTime();
        return Math.abs(diffInMillis) < DAY_IN_MILLIS && isSameDayOfMonth(date, now);
    }

    public static boolean isDayInFuture(Date date) {
        Date now = new Date();
        return date.after(now) && !isToday(date);
    }

    public static boolean isDayInPast(Date date) {
        Date now = new Date();
        return date.before(now) && !isToday(date);
    }
    
    public static int daysFromNow(Date date) {
        return (int) (getDayCount(date) - getDayCount(new Date()));
    }

    public static int daysUntilNow(Date date) {
        return (int) (getDayCount(new Date()) - getDayCount(date));
    }

    private static long getDayCount(Date date) {
        return date.getTime() / DAY_IN_MILLIS;
    }
}
