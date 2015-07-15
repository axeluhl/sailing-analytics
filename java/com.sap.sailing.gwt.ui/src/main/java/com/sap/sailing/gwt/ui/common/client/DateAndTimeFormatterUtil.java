package com.sap.sailing.gwt.ui.common.client;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.text.client.DateTimeFormatRenderer;

public class DateAndTimeFormatterUtil {
    public static DateTimeFormatRenderer defaultDateFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT));
    public static DateTimeFormatRenderer defaultTimeFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat(PredefinedFormat.TIME_LONG));

    public static DateTimeFormatRenderer longDateFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_LONG));
    public static DateTimeFormatRenderer longTimeFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat("HH:mm:ss zzz"));

    private static DateTimeFormatRenderer secondsTimeFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat("m:ss"));
    private static DateTimeFormatRenderer minutesAndSecondsTimeFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat("mm:ss"));
    private static DateTimeFormatRenderer hoursAndMinutesAndSecondsTimeFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat("HH:mm:ss"));

    public static String formatDateRange(Date startDate, Date endDate) {
        return defaultDateFormatter.render(startDate) + " - " + defaultDateFormatter.render(endDate);
    }

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
}
