package com.sap.sailing.android.tracking.app.nmea;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.location.Location;

@SuppressLint("SimpleDateFormat")
public class NmeaGprmcBuilder {
    /**
     * Sentence begin character
     */
    static char BEGIN_CHAR = '$';

    /**
     * Alternative sentence begin character used in VDO and VDM sentences.
     */
    static char ALTERNATIVE_BEGIN_CHAR = '!';

    /**
     * Checksum field delimiter char
     */
    static char CHECKSUM_DELIMITER = '*';

    /**
     * Sentence data fields delimiter char
     */
    static char FIELD_DELIMITER = ',';

    // The first two characters after '$'.
    static String TALKER_ID = "GP";

    // The next three characters after talker id.
    static final String SENTENCE_ID = "RMC";

    /**
     * Creates a GGA-Nmea String from an Location Object
     * 
     * @param location
     * @return
     */
    public static String buildNmeaStringFrom(Location location) {
        List<String> fields = parseInformationFromLocation(location);

        StringBuffer nmeaString = new StringBuffer(70);
        nmeaString.append(BEGIN_CHAR);
        nmeaString.append(TALKER_ID);
        nmeaString.append(SENTENCE_ID);

        for (String field : fields) {
            nmeaString.append(FIELD_DELIMITER);
            nmeaString.append(field);
        }

        nmeaString.append(CHECKSUM_DELIMITER);
        String checksum = calculateChecksumFrom(nmeaString.toString());
        nmeaString.append(checksum);
        return nmeaString.toString();
    }

    private static List<String> parseInformationFromLocation(Location location) {
        /*
         * RMC - NMEA has its own version of essential gps pvt (position, velocity, time) data. It is called RMC, The
         * Recommended Minimum, which will look similar to:
         * 
         * $GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A
         * 
         * Where: RMC Recommended Minimum sentence C 123519 Fix taken at 12:35:19 UTC A Status A=active or V=Void.
         * 4807.038,N Latitude 48 deg 07.038' N 01131.000,E Longitude 11 deg 31.000' E 022.4 Speed over the ground in
         * knots 084.4 Track angle in degrees True 230394 Date - 23rd of March 1994 003.1,W Magnetic Variation6A The
         * checksum data, always begins with *
         */

        List<String> fields = new ArrayList<String>();

        fields.add(getNmeaTimeStringFrom(location));
        fields.add(getNmeaStatusFrom(location));
        fields.add(getLatitudeFrom(location));
        fields.add(getLongitudeFrom(location));
        fields.add(getSpeedFrom(location));
        fields.add(getTrackAngleFrom(location));
        fields.add(getNmeaDateStringFrom(location));
        fields.add(getMagneticVariationFrom(location));

        return fields;
    }

    static String getMagneticVariationFrom(Location location) {
        // FIXME: is it possible to get the magnetic variation from an android location
        return "0,W";
    }

    static String getNmeaDateStringFrom(Location location) {
        SimpleDateFormat nmeaDateFormatter = new SimpleDateFormat("ddMMyy");
        nmeaDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String nmeaDateString = nmeaDateFormatter.format(new Date(location.getTime()));
        return nmeaDateString;
    }

    static String getTrackAngleFrom(Location location) {
        return "" + location.getBearing();
    }

    static String getLongitudeFrom(Location location) {
        double coordinate = location.getLongitude();
      //TODO Work arroud with replace , through .
        String convert = Location.convert(coordinate, Location.FORMAT_MINUTES).replace(",",".").replace(":","");
        double parseDouble = Double.parseDouble(convert);
        return convertLongitudeToPlanetocentric(parseDouble);
    }

    static String getLatitudeFrom(Location location) {
        double coordinate = location.getLatitude();
        //TODO Work arroud with replace , through .
        String convert = Location.convert(coordinate, Location.FORMAT_MINUTES).replace(",",".").replace(":", "");
        double parseDouble = Double.parseDouble(convert);
        return convertLatitudeToPlanetocentric(parseDouble);
    }

    static String getNmeaTimeStringFrom(Location location) {
        SimpleDateFormat nmeaTimeFormatter = new SimpleDateFormat("HHmmss");
        nmeaTimeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String nmeaTimeString = nmeaTimeFormatter.format(new Date(location.getTime()));
        return nmeaTimeString;
    }

    // FIXME: Nmea Status void or active?
    static String getNmeaStatusFrom(Location location) {
        return "A";
    }

    /**
     * Convert speed in m/s to knots
     * 
     * @param speed
     *            speed in m/s
     * @return speed in knots
     */
    static String getSpeedFrom(Location location) {
        double speed = location.getSpeed();
        // 1 international knot = 0.514 metres per second
        return "" + (speed / 0.514444);
    }

    private static String convertLongitudeToPlanetocentric(double longitude) {
        String longitudeString = "";
        if (longitude <= 0) {
            longitude = -longitude;
            longitudeString = longitude + ",W";
        } else {
            longitudeString = (longitude) + ",E";
        }
        if(longitude<1000){
            longitudeString = "00" + longitudeString;
        } else if(longitude<10000){
            longitudeString = "0" + longitudeString;
        }
        return longitudeString;
    }

    private static String convertLatitudeToPlanetocentric(double latitude) {
        String latitudeString = "";
        if (latitude <= 0) {
            latitude = -latitude;
            latitudeString = latitude + ",S";
        } else {
            latitudeString = (latitude) + ",N";
        }
        if(latitude<1000){
            latitudeString = "0" + latitudeString;
        }
        return latitudeString;
    }

    /**
     * Calculates the checksum of sentence String. Checksum is a XOR of each character between, but not including, the $
     * and * characters. The resulting hex value is returned as a String in two digit format, padded with a leading zero
     * if necessary. The method will calculate the checksum for any given String and the sentence validity is not
     * checked.
     * 
     * @param nmea
     *            NMEA Sentence with or without checksum.
     * @return Checksum hex value, padded with leading zero if necessary.
     */
    public static String calculateChecksumFrom(String nmea) {
        char ch;
        int sum = 0;
        for (int i = 0; i < nmea.length(); i++) {
            ch = nmea.charAt(i);
            if (i == 0 && (ch == BEGIN_CHAR || ch == ALTERNATIVE_BEGIN_CHAR)) {
                continue;
            } else if (ch == CHECKSUM_DELIMITER) {
                break;
            } else if (sum == 0) {
                sum = (byte) ch;
            } else {
                sum ^= (byte) ch;
            }
        }
        return String.format("%02X", sum);
    }
}
