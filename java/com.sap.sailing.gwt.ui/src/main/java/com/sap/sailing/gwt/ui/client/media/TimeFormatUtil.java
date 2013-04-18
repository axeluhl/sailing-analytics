package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.sap.sailing.gwt.ui.client.NumberFormatterFactory;

public class TimeFormatUtil {
    
    public static final DateTimeFormat DATETIME_FORMAT = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
    
    private static NumberFormat zeroPaddingNumberFormat_Min = NumberFormatterFactory.getDecimalFormat(2, 0);
    private static NumberFormat zeroPaddingNumberFormat_Sec = NumberFormatterFactory.getDecimalFormat(2, 3);

    public static int hrsMinSecToMilliSeconds(String hrsMinSec) {
        String[] segements = hrsMinSec.split(":");
        int milliseconds = 0;
        boolean isNegative = false;
        switch (segements.length) {
        case 3:
            String hoursText = segements[0].trim();
            int hours = Integer.valueOf(hoursText);
            isNegative = Integer.signum(hours) < 0;
            milliseconds = Math.abs(hours) * 60 * 60 * 1000;
        case 2:
            String minutesText = segements[segements.length - 2].trim();
            int minutes = Integer.valueOf(minutesText);
            isNegative = isNegative || Integer.signum(minutes) < 0;
            milliseconds = milliseconds + Math.abs(minutes) * 60 * 1000;
        case 1:
            String secondsText = segements[segements.length - 1].trim();
            float seconds = Float.valueOf(secondsText);
            isNegative = isNegative || Math.signum(seconds) < 0;
            milliseconds = milliseconds + Math.round(seconds * 1000);
        }
        if (isNegative) {
            return -milliseconds;
        } else {
            return milliseconds;
        }
    }

    public static String milliSecondsToHrsMinSec(long milliseconds) {
        int signum  = Long.signum(milliseconds);
        milliseconds = Math.abs(milliseconds);
        
        StringBuilder result = new StringBuilder();
        
        long hours = (milliseconds / (60 * 60 * 1000));
        if (hours > 0) {
            result.append(String.valueOf(signum * hours) + ':');
            signum = 1;
        }
        milliseconds = Math.abs(milliseconds);
        long rest = (milliseconds % (60 * 60 * 1000));
        long minutes = (rest / (60 * 1000));
        if (minutes > 0) {
            result.append(zeroPaddingNumberFormat_Min.format(signum * minutes) + ':');
            signum = 1;
        }
        rest = (rest % (60 * 1000));
        double seconds = rest / 1000d;
        result.append(zeroPaddingNumberFormat_Sec.format(signum * seconds));
        return result.toString();
    }

}
