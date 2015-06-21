package com.sap.sailing.gwt.ui.shared.util;

import java.util.Date;

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class ConditionalDateTimeFormatter {
    public static String format(Date newsTimestamp, Date currentTimestamp, StringMessages stringMessages) {
        String result;
        if (newsTimestamp.after(new Date(currentTimestamp.getTime() - 60000))) {
            result = stringMessages.now();
        } else {
            Date oneHourBeforeCurrent = new Date(currentTimestamp.getTime() - 3600000);
            if (newsTimestamp.after(oneHourBeforeCurrent)) {
                DateTimeFormat format = DateTimeFormat.getFormat("m");
                Date diffTime = new Date(currentTimestamp.getTime() - newsTimestamp.getTime());
                result = stringMessages.minutesAgo(format.format(diffTime));
            } else {
                if (sameDay(newsTimestamp, currentTimestamp)) {
                    DateTimeFormat format = DateTimeFormat.getFormat("h:m");
                    Date diffTime = new Date(currentTimestamp.getTime() - newsTimestamp.getTime());
                    result = stringMessages.hoursAgo(format.format(diffTime));
                } else {
                    result = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT).format(newsTimestamp);
                }
            }
        }
        return result;
    }

    private static boolean sameDay(Date timestamp1, Date timestamp2) {
        return CalendarUtil.isSameDate(timestamp1, timestamp2);
    }
}
