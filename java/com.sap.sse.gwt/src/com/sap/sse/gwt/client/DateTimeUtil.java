package com.sap.sse.gwt.client;

import java.util.Date;

import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsDurationImpl;

/**
 * Utility methods mainly for GWT clients for time/date features
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class DateTimeUtil {
    /**
     * @return a duration that is positive for time zones "west" of GMT/UTC and negative for those "east" of GMT/UTC,
     *         representing the client's time zone. Conceptually, this duration would have to be "added" to the client's
     *         local time to obtain GMT/UTC.
     */
    @SuppressWarnings("deprecation") // uses the deprecated Date.getTimezoneOffset() method but that's also what GWT does and seems the only way
    public static Duration getClientTimezoneOffsetFromUTC() {
        return new MillisecondsDurationImpl(new Date().getTimezoneOffset() /* that is in minutes */ * 60 * 1000);
    }
}
