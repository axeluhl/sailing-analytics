package com.sap.sailing.gwt.regattaoverview.client;

import java.util.Date;

public class DurationFormat {
    public String format(Date start, Date end) {
        long durationInMilliseconds = start.before(end) ? end.getTime() - start.getTime() : start.getTime()
                - end.getTime();
        return secondsToHms(durationInMilliseconds / 1000);
    }

    private String secondsToHms(long seconds) {
        long h = (long) Math.floor(seconds / 3600);
        long m = (long) Math.floor(seconds % 3600 / 60);
        long s = (long) Math.floor(seconds % 3600 % 60);
        return ((h > 0 ? (h < 10 ? "0" : "") + h + ":" : "00:") + (m > 0 ? (m < 10 ? "0" : "") + m + ":" : "00:")
                + (s < 10 ? "0" : "") + s);
    }
}
