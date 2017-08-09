package com.sap.sailing.gwt.ui.common.client;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.text.client.DateTimeFormatRenderer;

public class DateAndTimeFormatterUtil {
    public static TimeZone timeZoneWithoutOffset = TimeZone.createTimeZone(0);
    
    public static DateTimeFormatRenderer defaultDateFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT));
    public static DateTimeFormatRenderer defaultTimeFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat(PredefinedFormat.TIME_LONG));
    public static DateTimeFormatRenderer shortTimeFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat(PredefinedFormat.TIME_SHORT));
    public static DateTimeFormatRenderer mediumTimeFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat(PredefinedFormat.TIME_MEDIUM));

    public static DateTimeFormatRenderer weekdayMonthAbbrDayDateFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat("EEE, " + LocaleInfo.getCurrentLocale().getDateTimeFormatInfo().formatMonthAbbrevDay()));
    public static DateTimeFormatRenderer longDateFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_LONG));
    public static DateTimeFormatRenderer longTimeFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat("HH:mm:ss zzz"), timeZoneWithoutOffset);
    public static DateTimeFormatRenderer minutesTimeFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat("m"), timeZoneWithoutOffset);
    public static DateTimeFormatRenderer hoursAndMinutesTimeFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat("HH:mm"), timeZoneWithoutOffset);

    private static DateTimeFormatRenderer secondsTimeFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat("m:ss"), timeZoneWithoutOffset);
    private static DateTimeFormatRenderer minutesAndSecondsTimeFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat("mm:ss"), timeZoneWithoutOffset);
    private static DateTimeFormatRenderer hoursAndMinutesAndSecondsTimeFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat("HH:mm:ss"), timeZoneWithoutOffset);

    public static String formatDateRange(Date startDate, Date endDate) {
        return defaultDateFormatter.render(startDate) + " - " + defaultDateFormatter.render(endDate);
    }

    /**
     * Formats a duration in a compact format so that hours and minutes are only shown if the duration is >1h to avoid
     * unnecessary 00:yy or 00:xx:yy values.
     * 
     * Be aware that this method doesn't work for durations >= 24h.
     * 
     * @param timeInMilliseconds
     *            the duration in milliseconds to format
     * @return the formatted duration
     */
    public static String formatElapsedTime(long timeInMilliseconds) {
        String result = "";
        int seconds = (int) (timeInMilliseconds / 1000) % 60 ;
        int minutes = (int) ((timeInMilliseconds / (1000*60)) % 60);
        int hours   = (int) ((timeInMilliseconds / (1000*60*60)) % 24);
        if(hours > 0) {
            result = hoursAndMinutesAndSecondsTimeFormatter.render(new Date(timeInMilliseconds));
        } else if (minutes > 0) {
            result = minutesAndSecondsTimeFormatter.render(new Date(timeInMilliseconds));
        } else if (seconds > 0) {
            result = secondsTimeFormatter.render(new Date(timeInMilliseconds));
        }
        
        return result;
    }
    
    public static String formatDateAndTime(Date date) {
        String result = "";
        if(date != null) {
            result = defaultDateFormatter.render(date) + " " + defaultTimeFormatter.render(date);
        }
        
        return result;
    }
    
    public static String formatLongDateAndTimeGMT(Date date) {
        return longDateFormatter.render(date) + ", " + longTimeFormatter.render(date);
    }

    public static String getClientTimeZoneAsGMTString() {
        Date now = new Date();
        @SuppressWarnings("deprecation")
        int localeTimezoneOffset = now.getTimezoneOffset();
        TimeZone localeTimeZone = TimeZone.createTimeZone(localeTimezoneOffset);
        return localeTimeZone.getGMTString(now);
    }
}
