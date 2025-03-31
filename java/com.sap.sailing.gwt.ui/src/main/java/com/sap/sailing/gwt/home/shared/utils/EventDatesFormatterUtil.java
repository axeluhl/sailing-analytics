package com.sap.sailing.gwt.home.shared.utils;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.shared.utils.dateformatter.DefaultEventDatesFormatter;

/**
 * Formatter for event dates and date ranges
 * @author Frank
 *
 */
public class EventDatesFormatterUtil {
    
    private final static DefaultEventDatesFormatter FORMATTER = GWT.create(DefaultEventDatesFormatter.class);
    
    public static String formatDateRangeWithYear(Date from, Date to) {
        return (from == null || to == null) ? "" : FORMATTER.formatDateRangeWithYear(from, to);
    }

    public static String formatDateRangeWithoutYear(Date from, Date to) {
        return (from == null || to == null) ? "" : FORMATTER.formatDateRangeWithoutYear(from, to);
    }
}
