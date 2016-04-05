package com.sap.sailing.gwt.home.shared.utils.dateformatter;

import java.util.Date;

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;

public class DefaultEventDatesFormatter {
    
    private static final DateTimeFormat FORMAT_MONTH_DAY = DateTimeFormat.getFormat(PredefinedFormat.MONTH_DAY);
    private static final DateTimeFormat FORMAT_YEAR_MONTH_DAY = DateTimeFormat.getFormat(PredefinedFormat.YEAR_MONTH_DAY);

    public String formatDateRangeWithYear(Date from, Date to) {
        return format(FORMAT_YEAR_MONTH_DAY, from, to);
    }
    
    public String formatDateRangeWithoutYear(Date from, Date to) {
        return format(FORMAT_MONTH_DAY, from, to);
    }
    
    private String format(DateTimeFormat formatter, Date from, Date to) {
        return formatter.format(from) + " - "  + formatter.format(to);
    }
    
}
