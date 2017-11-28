package com.sap.sailing.gwt.ui.client;

import com.google.gwt.i18n.client.NumberFormat;

public class DurationAsHoursMinutesSecondsFormatter {
    public String getHoursMinutesSeconds(Double durationInSeconds) {
        String result;
        if (durationInSeconds == null) {
            result = null;
        } else {
            int hh = (int) (durationInSeconds / 3600);
            int mm = (int) ((durationInSeconds - 3600 * hh) / 60);
            int ss = (int) (durationInSeconds - 3600 * hh - 60 * mm);
            NumberFormat numberFormat = NumberFormatterFactory.getDecimalFormat(2, 0);
            result = "" + numberFormat.format(hh) + ":" + numberFormat.format(mm) + ":" + numberFormat.format(ss);
        }
        return result;
    }
}
