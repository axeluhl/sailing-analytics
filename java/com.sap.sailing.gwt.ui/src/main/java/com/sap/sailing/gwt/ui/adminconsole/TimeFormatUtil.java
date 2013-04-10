package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.sap.sailing.gwt.ui.client.NumberFormatterFactory;

public class TimeFormatUtil {
    
    public static final DateTimeFormat DATETIME_FORMAT = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
    
    private static NumberFormat zeroPaddingNumberFormat_Min = NumberFormatterFactory.getDecimalFormat(2, 0);
    private static NumberFormat zeroPaddingNumberFormat_Sec = NumberFormatterFactory.getDecimalFormat(2, 3);

    public static int hrsMinSecToMilliSeconds(String hrsMinSec) {
        String[] segements = hrsMinSec.split(":");
        int milliseconds = 0;
        switch (segements.length) {
        case 3:
            String hours = segements[0].trim();
            milliseconds = Integer.valueOf(hours) * 60 * 60 * 1000;
        case 2:
            String minutes = segements[segements.length - 2].trim();
            milliseconds = milliseconds + Integer.valueOf(minutes) * 60 * 1000;
        case 1:
            String seconds = segements[segements.length - 1].trim();
            milliseconds = milliseconds + Math.round(Float.valueOf(seconds) * 1000);
        }
        return milliseconds;
    }

    public static String milliSecondsToHrsMinSec(long milliseconds) {
        long hours = (milliseconds / (60 * 60 * 1000));
        long rest = (milliseconds % (60 * 60 * 1000));
        long minutes = (rest / (60 * 1000));
        rest = (rest % (60 * 1000));
        double seconds = (double) rest / 1000;
        String result = String.valueOf(hours) + ':' + zeroPaddingNumberFormat_Min.format(minutes) + ':' + zeroPaddingNumberFormat_Sec.format(seconds);
        return result;
    }

}
