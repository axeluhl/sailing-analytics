package com.sap.sse.util.apachelog;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Represents a line of the form
 * <pre>
 * ess2017.sapsailing.com 91.7.27.233 - - [15/Dec/2017:16:49:41 +0000] "POST /gwt/service/sailing HTTP/1.1" 200 437 "https://ess2017.sapsailing.com/gwt/RaceBoard.html?regattaName=ESS+2017+Los+Cabos&raceName=Race+1&leaderboardName=ESS+2017+Los+Cabos&leaderboardGroupName=Extreme+Sailing+Series+2017&eventId=6eb987e5-1334-45f9-b54a-8b772a3fbb5b&mode=PLAYER" "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36"
 * </pre>
 * with its constituents. It is parsed by splitting by space, respecting double-quoted strings which then may contain spaces.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class LogEntry {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss ZZZZZ");
    
    private final String hostname;
    private final String requestorIpString;
    private final String remoteLogname;
    private final String remoteUser;
    private final String timestampString;
    private final String request;
    private final String httpStatusCodeString;
    private final String sizeString;
    private final String referrer;
    private final String userAgent;
    
    public LogEntry(String logEntry) {
        final Iterator<String> i = Util.splitAlongWhitespaceRespectingDoubleQuotedPhrases(logEntry).iterator();
        hostname = i.next().trim();
        requestorIpString = i.next();
        remoteLogname = i.next();
        remoteUser = i.next();
        final String dateTimeWithOpeningBracket = i.next();
        final String timeZoneOffsetWithClosingBracket = i.next();
        timestampString = dateTimeWithOpeningBracket.substring(1) + " " + timeZoneOffsetWithClosingBracket.substring(0, timeZoneOffsetWithClosingBracket.length()-1);
        request = i.next();
        httpStatusCodeString = i.next();
        sizeString = i.next();
        referrer = i.next();
        userAgent = i.next();
    }

    public String getHostname() {
        return hostname;
    }

    public String getRequestorIpString() {
        return requestorIpString;
    }
    
    public InetAddress getRequestorIp() throws UnknownHostException {
        return InetAddress.getByName(getRequestorIpString());
    }

    public String getRemoteLogname() {
        return remoteLogname;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public String getTimestampString() {
        return timestampString;
    }
    
    /**
     * Returns only the date-specific portion of the {@link #getTimestampString() timestamp string}. For example,
     * "15/Dec/2017:16:49:41 +0000" will result in "15/Dec/2017".
     */
    public String getDateString() {
        return getTimestampString().substring(0, getTimestampString().indexOf(':'));
    }
    
    public TimePoint getTimepoint() throws ParseException {
        return new MillisecondsTimePoint(dateFormat.parse(getTimestampString()));
    }

    public String getRequest() {
        return request;
    }

    public String getHttpStatusCodeString() {
        return httpStatusCodeString;
    }
    
    public int getHttpStatusCode() {
        return Integer.valueOf(getHttpStatusCodeString());
    }

    public String getSizeString() {
        return sizeString;
    }
    
    public long getSize() {
        return Long.valueOf(getSizeString());
    }

    public String getReferrer() {
        return referrer;
    }

    public String getUserAgent() {
        return userAgent;
    }

}
