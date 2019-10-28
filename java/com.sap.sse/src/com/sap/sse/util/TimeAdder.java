package com.sap.sse.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.SecondsDurationImpl;

/**
 * Provide an ISO timestamp first, then a duration in hh:mm:ss format. The time point resulting from
 * adding the latter to the former will be sent to {@code stdout}.<p>
 * 
 * Sample usage:
 * <pre>
 *     $ java -jar TimeAdder.jar 2019-10-13T16:43:00+0200 24:18:57
 *     Race start Sun Oct 13 16:43:00 CEST 2019 plus elapsed time 24:18:57.000 gives finishing time Mon Oct 14 17:01:57 CEST 2019
 * </pre>
 * @author Axel Uhl (D043530)
 *
 */
public class TimeAdder {
    public static void main(String[] args) throws ParseException {
        if (args.length != 2) {
            usage();
        } else {
            final String startOfRaceAsString = args[0];
            final String elapsedTimeAsString = args[1];
            final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
            final TimePoint startOfRace = new MillisecondsTimePoint(formatter.parse(startOfRaceAsString));
            final String[] elapsedTimeAsHhmmss = elapsedTimeAsString.split(":");
            final Duration elapsedTime = new SecondsDurationImpl(3600*Integer.valueOf(elapsedTimeAsHhmmss[0]) +
                    60*Integer.valueOf(elapsedTimeAsHhmmss[1]) + Integer.valueOf(elapsedTimeAsHhmmss[2]));
            System.out.println("Race start "+startOfRace+" plus elapsed time "+elapsedTime+" gives finishing time "+
                    startOfRace.plus(elapsedTime));
        }
    }

    private static void usage() {
        System.err.println("Provide an ISO timestamp first, then a duration in hh:mm:ss format. The time point resulting from\r\n" + 
                "adding the latter to the former will be sent to {@code stdout}.<p>\r\n" + 
                "\r\n" + 
                "Sample usage:\r\n" + 
                "\r\n" + 
                "    $ java -jar TimeAdder.jar 2019-10-13T16:43:00+0200 24:18:57\r\n" + 
                "    Race start Sun Oct 13 16:43:00 CEST 2019 plus elapsed time 24:18:57.000 gives finishing time Mon Oct 14 17:01:57 CEST 2019\r\n"); 
    }
}
