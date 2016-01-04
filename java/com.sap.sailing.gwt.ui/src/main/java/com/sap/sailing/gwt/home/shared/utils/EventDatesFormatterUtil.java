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

    public static String formatDateRangeWithYear(Date from, Date to) {
        final String result;
        final String fromMonth = monthFormat.format(from);
        final String toMonth = monthFormat.format(to);
        if (fromMonth.equals(toMonth)) {
            // same month
            final String fromDay = dayFormat.format(from);
            final String toDay = dayFormat.format(to);
            if (fromDay.equals(toDay)) {
                // same day
                result = fromMonth + " " + fromDay + ", " + yearFormat.format(from);
            } else {
                result = fromMonth + " " + fromDay + " - " + toDay + ", "
                        + yearFormat.format(from);
            }
        } else {
            result = dayAndMonthFormat.format(from) + " - " + dayAndMonthFormat.format(to) + ", "
                    + yearFormat.format(from);
        }
        return result;
    }

    public static String formatDateRangeWithoutYear(Date from, Date to) {
        final String result;
        final String fromMonth = monthFormat.format(from);
        final String toMonth = monthFormat.format(to);
        if (fromMonth.equals(toMonth)) {
            // same month
            final String fromDay = dayFormat.format(from);
            final String toDay = dayFormat.format(to);
            if (fromDay.equals(toDay)) {
                // same day
                result = fromMonth + " " + fromDay;
            } else {
                result = fromMonth + " " + fromDay + " - " + toDay;
            }
        } else {
            result = dayAndMonthFormat.format(from) + " - " + dayAndMonthFormat.format(to);
        }
        return result;
    }
}
