package com.sap.sailing.server.gateway.windimport.expedition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class WindLogParser {
    /**
     * Collects partial wind data until everything is there to create a Wind instance.
     * 
     * @author D047974
     *
     */
    private static class WindBuffer {
        /**
         * The 1900 Date System http://support.microsoft.com/kb/180162/en-us
         */
        private static final Calendar EXCEL_EPOCH_START = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        static {
            EXCEL_EPOCH_START.set(1899, Calendar.DECEMBER, 30, 0, 0, 0);
            EXCEL_EPOCH_START.set(Calendar.MILLISECOND, 0);
        }
        private static final BigDecimal MILLISECONDS_PER_DAY = BigDecimal.valueOf(24 * 60 * 60 * 1000);

        private TimePoint timePoint;
        private Position position;
        private SpeedWithBearing trueWindSpeedWithBearing;

        public void updateTime(String time_ExcelEpoch) {
            if (!time_ExcelEpoch.trim().isEmpty()) {
                BigDecimal timeStamp = new BigDecimal(time_ExcelEpoch);
                long millisecondsSinceExcelEpochStart = timeStamp.multiply(MILLISECONDS_PER_DAY).longValue();
                this.timePoint = new MillisecondsTimePoint(
                        EXCEL_EPOCH_START.getTimeInMillis() + millisecondsSinceExcelEpochStart);
            }
        }

        public void updateWindData(String trueWindSpeedData, String trueWindDirectionData) {
            if (!trueWindSpeedData.trim().isEmpty() && !trueWindDirectionData.trim().isEmpty()) {
                double trueWindSpeed = Double.parseDouble(trueWindSpeedData);
                double trueWindDirection = Double.parseDouble(trueWindDirectionData);
                Bearing trueWindBearing = new DegreeBearingImpl(trueWindDirection + 180);
                this.trueWindSpeedWithBearing = new KnotSpeedWithBearingImpl(trueWindSpeed, trueWindBearing);
            }
        }

        public void updatePosition(String latData, String lonData) {
            if (!latData.trim().isEmpty() && !lonData.trim().isEmpty()) {
                double lat = Double.parseDouble(latData);
                double lon = Double.parseDouble(lonData);
                this.position = new DegreePosition(lat, lon);
            }
        }

        public Wind createWindIfReady() {
            if ((timePoint != null) && (position != null) && (trueWindSpeedWithBearing != null)) {
                WindImpl result = new WindImpl(position, timePoint, trueWindSpeedWithBearing);
                reset();
                return result;
            } else {
                return null;
            }
        }

        private void reset() {
            timePoint = null;
            position = null;
            trueWindSpeedWithBearing = null;
        }
    }

    private static final String COL_NAME_TIME_STAMP = "GPS Time";
    private static final String COL_NAME_TRUE_WIND_SPEED = "Tws";
    private static final String COL_NAME_TRUE_WIND_DIRECTION = "Twd";
    private static final String COL_NAME_LAT = "Lat";
    private static final String COL_NAME_LONG = "Lon";
    
    private static class ColumnNumbers {
        Integer gpsTime;
        Integer trueWindSpeed;
        Integer trueWindDirection;
        Integer lat;
        Integer lon;
        final int max;

        ColumnNumbers(String headerLine) {
            if (headerLine == null) {
                throw new NullPointerException("Empty header line.");
            }
            //supposed to be like Utc,Bsp,Awa,Aws,Twa,Tws,Twd,Rudder2,Leeway,Set,Drift,Hdg,AirTmp,SeaTmp,Baro,Depth,Heel,Trim,Rudder,Tab,Forestay,Downhaul,MastAng,FrstyLen,MastButt,LoadStbd,LoadPort,Rake,Volts,ROT,GpsQual,GpsPDOP,GpsNum,GpsAge,GpsAltitude,GpsGeoSep,GpsMode,Lat,Lon,Cog,Sog,DiffStn,Error,StbRunner,PrtRunner,Vang,Traveller,MainSheet,KeelAng,KeelHt,CanardH,OilPres,RPM1,RPM2,Dagger Bd,Boom Pos,DistToLn,RchTmToLn,RchDtToLn,GPS Time,Downhaul2,MkLat,MkLon,PortLat,PortLon,StbdLat,StbdLon,GPS HPE,RH,LeadPt,LeadSb,BkStay,User 0,User 1,User 2,User 3,User 4,User 5,User 6,User 7,User 8,User 9,User 10,User 11,User 12,User 13,User 14,User 15,User 16,User 17,User 18,User 19,User 20,User 21,User 22,User 23,User 24,User 25,User 26,User 27,User 28,User 29,User 30,User 31,TmToGun,TmToLn,TmToBurn,BelowLn,GunBlwLn,WvSigHt,WvSigPd,WvMaxHt,WvMaxPd,Slam,Motion
            //there may be slight difference from one Expedition version to another, but so far not in the wind-related columns 
            String[] columns = LINE_PATTERN.split(headerLine);
            for (int i = 0; i < columns.length; i++) {
                switch (columns[i]) {
                case COL_NAME_TIME_STAMP:
                    gpsTime = i;
                    break;
                case COL_NAME_TRUE_WIND_SPEED:
                    trueWindSpeed = i;
                    break;
                case COL_NAME_TRUE_WIND_DIRECTION:
                    trueWindDirection = i;
                    break;
                case COL_NAME_LAT:
                    lat = i;
                    break;
                case COL_NAME_LONG:
                    lon = i;
                    break;
                }
            }
            if (gpsTime == null || trueWindSpeed == null || trueWindDirection == null || lat == null || lon == null) {
                throw new RuntimeException("Unexpected csv header for Expedition wind import: " + headerLine);
            } else {
                max = Collections.max(Arrays.asList(gpsTime, trueWindSpeed, trueWindDirection, lat, lon));
            }
        }
    }

    private static final Pattern LINE_PATTERN = Pattern.compile(",");

    /**
     * See ExpeditionMessage, UDPExpeditionReceiver
     * 
     * @param windStream
     * @return
     * @throws IOException
     */
    public static Iterable<Wind> importWind(InputStream windStream) throws IOException {
        BufferedReader csvReader = new BufferedReader(new InputStreamReader(windStream));
        String headerLine = csvReader.readLine();
        ColumnNumbers columnNumbers = new ColumnNumbers(headerLine);
        List<Wind> result = new ArrayList<Wind>();
        String dataLine;
        WindBuffer windBuffer = new WindBuffer();
        while ((dataLine = csvReader.readLine()) != null) {
            String[] data = parseDataLine(dataLine, columnNumbers.max);
            if (data != null) {
                windBuffer.updateTime(data[columnNumbers.gpsTime]);
                windBuffer.updateWindData(data[columnNumbers.trueWindSpeed], data[columnNumbers.trueWindDirection]);
                windBuffer.updatePosition(data[columnNumbers.lat], data[columnNumbers.lon]);
                Wind wind = windBuffer.createWindIfReady();
                if (wind != null) {
                    result.add(wind);
                }
            }
        }
        return result;
    }

    private static String[] parseDataLine(String dataLine, int minLength) {
        String[] data = LINE_PATTERN.split(dataLine);
        if (data.length >= minLength) {
            return data;
        }
        // else
        return null;
    }
}
