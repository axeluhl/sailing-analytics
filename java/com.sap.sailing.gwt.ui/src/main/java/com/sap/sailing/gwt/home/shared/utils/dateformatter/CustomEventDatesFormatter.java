package com.sap.sailing.gwt.home.shared.utils.dateformatter;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

public class CustomEventDatesFormatter extends DefaultEventDatesFormatter {

    private final static DateTimeFormat FORMAT_DAY = DateTimeFormat.getFormat("dd");
    private final static DateTimeFormat FORMAT_MONTH_DAY = DateTimeFormat.getFormat("MMMM dd");
    private final static DateTimeFormat FORMAT_MONTH = DateTimeFormat.getFormat("MMMM");
    private final static DateTimeFormat FORMAT_YEAR = DateTimeFormat.getFormat("yyyy");

    public String formatDateRangeWithYear(Date from, Date to) {
        final String fromYear = FORMAT_YEAR.format(from), toYear = FORMAT_YEAR.format(to);
        if (fromYear.equals(toYear)) { // same year
            return formatDateRangeWithoutYear(from, to) + ", " + fromYear;
        }
        return FORMAT_MONTH_DAY.format(from) + ", " + fromYear + " - " + FORMAT_MONTH_DAY.format(to) + ", " + toYear;
    }

    public String formatDateRangeWithoutYear(Date from, Date to) {
        final String fromMonth = FORMAT_MONTH.format(from), toMonth = FORMAT_MONTH.format(to);
        if (fromMonth.equals(toMonth)) { // same month
            final String fromDay = FORMAT_DAY.format(from), toDay = FORMAT_DAY.format(to);
            return fromMonth + " " + fromDay + (fromDay.equals(toDay) /* same day*/ ? "" : (" - " + toDay));
        }
        return FORMAT_MONTH_DAY.format(from) + " - " + FORMAT_MONTH_DAY.format(to);
    }
}
