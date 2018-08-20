package com.sap.sailing.server.gateway.windimport.bravo;

/**
 * Latitude / longitude in Bravo files are represented in funny NMEA-like way; the value divided by 100 as a floored
 * integer represents the full degrees; the value modulo 100 represents the decimal minutes. Example: the pair
 * (4124.645890, 213.738670) stands for N41°24.645890 E002°13.738670, or as decimal degrees (41.410765, 2.228978)
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class FunnyDegreeConverter {
    /**
     * Latitude / longitude in Bravo files are represented in funny NMEA-like way; the value divided by 100 as a floored integer
     * represents the full degrees; the value modulo 100 represents the decimal minutes. Example: the pair (4124.645890,
     * 213.738670) stands for N41°24.645890 E002°13.738670, or as decimal degrees (41.410765, 2.228978)
     * 
     * @param d double value in "funny" format
     * @return double value as decimal degrees
     */
    public static double funnyLatLng(double d) {
        final int intDeg = (int) (d / 100.);
        final double minutes = d - 100*intDeg;
        return ((double) intDeg) + minutes/60.;
    }
}
