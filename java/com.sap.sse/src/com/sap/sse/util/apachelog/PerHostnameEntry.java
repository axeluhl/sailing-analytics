package com.sap.sse.util.apachelog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TimeZone;

import com.sap.sse.common.Util;

public class PerHostnameEntry {
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy");
    private final String requestorIpString;
    private final String dateString;
    private final String userAgent;
    private final int year;
    private final int zeroBasedMonth;
    
    /**
     * Parses a line of format "66.249.66.14 17/Dec/2017 Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"
     * assuming that IP and date and useragent are separated by spaces and the user agent string fills up the remainder of the line.
     */
    public PerHostnameEntry(String line) {
        final Iterator<String> i = Util.splitAlongWhitespaceRespectingDoubleQuotedPhrases(line).iterator();
        requestorIpString = i.next();
        dateString = i.next();
        final StringBuilder userAgentBuilder = new StringBuilder();
        while (i.hasNext()) {
            userAgentBuilder.append(i.next());
            if (i.hasNext()) {
                userAgentBuilder.append(' ');
            }
        }
        userAgent = userAgentBuilder.toString();
        final Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        int tmpYear;
        int tmpZeroBasedMonth;
        try {
            cal.setTime(dateFormat.parse(dateString));
            tmpYear = cal.get(Calendar.YEAR);
            tmpZeroBasedMonth = cal.get(Calendar.MONTH);
        } catch (ParseException e) {
            tmpYear = 0;
            tmpZeroBasedMonth = 0;
        }
        zeroBasedMonth = tmpZeroBasedMonth;
        year = tmpYear;
    }

    public String getRequestorIpString() {
        return requestorIpString;
    }

    public String getDateString() {
        return dateString;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public int getYear() {
        return year;
    }

    public int getZeroBasedMonth() {
        return zeroBasedMonth;
    }
}
