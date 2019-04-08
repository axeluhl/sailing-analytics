package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.NumberFormat;
import com.sap.sailing.gwt.ui.client.NumberFormatterFactory;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsDurationImpl;

public class TimeFormatUtil {
    
    static final int MILLISECONDS_PER_MINUTE = 60 * 1000;

    static final int MILLISECONDS_PER_HOUR = 60 * MILLISECONDS_PER_MINUTE;

    public static final DateTimeFormat DATETIME_FORMAT = DateTimeFormat.getFormat("yyyy MMM dd HH:mm:ss.SSS");
    
    private static NumberFormat zeroPaddingNumberFormat_Min = NumberFormatterFactory.getDecimalFormat(2, 0);
    private static NumberFormat zeroPaddingNumberFormat_Sec = NumberFormat.getFormat("00.000");

    public static Duration hrsMinSecToMilliSeconds(String hrsMinSec) {
        String[] segements = hrsMinSec.split(":");
        long milliseconds = 0;
        boolean isNegative = false;
        switch (segements.length) {
        case 3:
            String hoursText = segements[0].trim();
            int hours = Integer.valueOf(hoursText);
            isNegative = Integer.signum(hours) < 0;
            milliseconds = Math.abs(hours) * MILLISECONDS_PER_HOUR;
        case 2:
            String minutesText = segements[segements.length - 2].trim();
            int minutes = Integer.valueOf(minutesText);
            isNegative = isNegative || Integer.signum(minutes) < 0;
            milliseconds = milliseconds + Math.abs(minutes) * MILLISECONDS_PER_MINUTE;
        case 1:
            String secondsText = segements[segements.length - 1].trim();
            float seconds = Float.valueOf(secondsText);
            isNegative = isNegative || Math.signum(seconds) < 0;
            milliseconds = milliseconds + Math.round(seconds * 1000);
        }
        if (isNegative) {
            return new MillisecondsDurationImpl(-milliseconds);
        } else {
            return new MillisecondsDurationImpl(milliseconds);
        }
    }

    public static String durationToHrsMinSec(Duration duration) {
        if (duration != null) {
            long milliseconds = duration.asMillis();
            int signum  = Long.signum(milliseconds);
            milliseconds = Math.abs(milliseconds);
            
            StringBuilder result = new StringBuilder();
            
            long hours = (milliseconds / MILLISECONDS_PER_HOUR);
            if (hours > 0) {
                result.append(String.valueOf(signum * hours) + ':');
                signum = 1;
            }
            milliseconds = Math.abs(milliseconds);
            long rest = (milliseconds % MILLISECONDS_PER_HOUR);
            long minutes = (rest / MILLISECONDS_PER_MINUTE);
            if (minutes > 0 || result.length() > 0) {
                result.append(zeroPaddingNumberFormat_Min.format(signum * minutes) + ':');
                signum = 1;
            }
            rest = (rest % MILLISECONDS_PER_MINUTE);
            double seconds = rest / 1000d;
            double toFormat = signum * seconds;
            String localeSpecificFormatedValue = zeroPaddingNumberFormat_Sec.format(toFormat);
            String currentLocaleSeperator = LocaleInfo.getCurrentLocale().getNumberConstants().decimalSeparator();
            result.append(localeSpecificFormatedValue.replace(currentLocaleSeperator, "."));
            return result.toString();
        } else {
            return "";
        }
    }

}
