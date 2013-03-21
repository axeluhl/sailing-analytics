package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.text.client.DateTimeFormatRenderer;

public class DateAndTimeFormatterUtil {
    public static DateTimeFormatRenderer defaultDateFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT));
    public static DateTimeFormatRenderer defaultTimeFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat(PredefinedFormat.TIME_LONG));

    public static String formatDateAndTime(Date date) {
        String result = "";
        if(date != null) {
            result = defaultDateFormatter.render(date) + " " + defaultTimeFormatter.render(date);
        }
        
        return result;
    }
}
