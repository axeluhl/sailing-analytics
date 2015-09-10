package com.sap.sailing.gwt.home.shared.utils;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * Formatter for event dates and date ranges
 * @author Frank
 *
 */
public class EventDatesFormatterUtil {
    private final static DateTimeFormat dayFormat = DateTimeFormat.getFormat("dd");
    private final static DateTimeFormat dayAndMonthFormat = DateTimeFormat.getFormat("MMMM dd");
    private final static DateTimeFormat monthFormat = DateTimeFormat.getFormat("MMMM");
    private final static DateTimeFormat yearFormat = DateTimeFormat.getFormat("yyyy");

    private final static int ONE_DAY = 24 * 60 * 60 * 1000; 

    @SuppressWarnings("deprecation")
    public static String formatDateRangeWithYear(Date from, Date to) {
        String result = "";
        if(from.getMonth() == to.getMonth()) {
            // same month
            if(from.getDay() == to.getDay() && to.getTime() - to.getTime() <= ONE_DAY) {
                // same day
                result = monthFormat.format(from) + " " + dayFormat.format(from) + ", " + yearFormat.format(from);
            } else {
                result = monthFormat.format(from) + " " + dayFormat.format(from) + " - " + dayFormat.format(to) + ", " + yearFormat.format(from);
            }
        } else {
            result = dayAndMonthFormat.format(from) + " - " + dayAndMonthFormat.format(to) + ", " + yearFormat.format(from);
        }
                
        return result;
    }

    @SuppressWarnings("deprecation")
    public static String formatDateRangeWithoutYear(Date from, Date to) {
        String result = "";
        if(from.getMonth() == to.getMonth()) {
            // same month
            if(from.getDay() == to.getDay() && to.getTime() - to.getTime() <= ONE_DAY) {
                // same day
                result =  monthFormat.format(from) + " " + dayFormat.format(from);
            } else {
                result =  monthFormat.format(from) + " " + dayFormat.format(from) + " - " + dayFormat.format(to);
            }
        } else {
            result = dayAndMonthFormat.format(from) + " - " + dayAndMonthFormat.format(to);
        }
                
        return result;
    }

}
