package com.sap.sailing.domain.orc.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.common.orc.impl.ORCCertificateImpl;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.SecondsDurationImpl;

/**
 * Represents a file in format {@code .rms} which is a simple ASCII file format, column-based, with fixed-width columns,
 * defined by a header line that defines the names and for the first columns up to the column labeled
 * {@link #NAME_OF_LAST_LEFT_ALIGNED_COLUMN_HEADER} also the width of the columns. The header uses column names that do
 * not contain spaces, separated by one or more spaces. For the columns up to and including the column
 * {@link #NAME_OF_LAST_LEFT_ALIGNED_COLUMN_HEADER} the column names are formatted left-aligned. For subsequent columns
 * which contain all numeric values without spaces the column names may also be centered. For those columns parsing can
 * simply split by space characters.
 * <p>
 * 
 * The result of successfully parsing a {@code .rms} file is a map keyed by the sailnumber, with values being
 * equal-sized maps from the column names to the {@link String} values. Additionally, the column names corresponding to
 * the array indices can be queried. A further step involves the creation of {@link ORCCertificate}s from the values of
 * the map.
 * 
 * @author Axel Uhl (d043530)
 * @author Daniel Lisunkin (i505543)
 *
 */

public class ORCCertificatesCollectionRMS extends AbstractORCCertificatesCollection {
    // Codes used in the RMS document as column names for different information
    private static final String TWA_COURSES = "R";
    private static final String CDL = "CDL";
    private static final String GPH = "GPH";
    private static final String LENGTH = "LOA";
    private static final String BOATCLASS = "TYPE";
    private static final String BOATNAME = "NAME";
    private static final String RUN_ALLOWANCE = "D";
    private static final String RUN_ANGLE = "DA";
    private static final String BEAT_ALLOWANCE = "UP";
    private static final String BEAT_ANGLE = "UA";
    private static final String WINDWARD_LEEWARD = "WL";
    private static final String LONG_DISTANCE = "OC";
    private static final String CIRCULAR_RANDOM = "CR";
    private static final String NON_SPINNAKER = "NSP";
    private static final String NATCERTN_FILE_ID = "NATCERTN.FILE_ID";

    private final Map<String, Map<String, String>> certificateValuesBySailnumber;

    public class ORCCertificateValues {
        private final String sailnumber;

        public ORCCertificateValues(String sailnumber) {
            super();
            this.sailnumber = sailnumber;
        }

        public String getValue(String columnName) {
            return certificateValuesBySailnumber.get(sailnumber).get(columnName);
        }
    }
    
    public ORCCertificatesCollectionRMS(Map<String, Map<String, String>> certificateValuesBySailnumber) throws IOException {
        this.certificateValuesBySailnumber = new HashMap<>();
        for (final Entry<String, Map<String, String>> e : certificateValuesBySailnumber.entrySet()) {
            this.certificateValuesBySailnumber.put(getCanonicalizedSailNumber(e.getKey()), e.getValue());
        }
    }
    
    public Set<String> getSailnumbers() {
        return Collections.unmodifiableSet(certificateValuesBySailnumber.keySet());
    }

    private ORCCertificateValues getValuesForSailnumber(String sailnumber) {
        String searchString = getCanonicalizedSailNumber(sailnumber);
        return certificateValuesBySailnumber.containsKey(searchString) ? new ORCCertificateValues(searchString) : null;
    }
    
    @Override
    public ORCCertificate getCertificateBySailNumber(String sailnumber) {
        String searchString = getCanonicalizedSailNumber(sailnumber);
        ORCCertificateValues certificateValues = getValuesForSailnumber(searchString);
        final String boatclass = certificateValues.getValue(BOATCLASS);
        final String boatName = certificateValues.getValue(BOATNAME);
        final Distance length  = new MeterDistance(Double.parseDouble(certificateValues.getValue(LENGTH)));
        final Duration gph     = new SecondsDurationImpl(Double.parseDouble(certificateValues.getValue(GPH)));
        final Double cdl       = Double.parseDouble(certificateValues.getValue(CDL));
        final Map<Speed, Map<Bearing, Speed>> velocityPredictionsPerTrueWindSpeedAndAngle = new HashMap<>();
        final Map<Speed, Bearing> beatAngles = new HashMap<>();
        final Map<Speed, Speed> beatVMGPredictionPerTrueWindSpeed = new HashMap<>();
        final Map<Speed, Duration> beatAllowancePerTrueWindSpeed = new HashMap<>();
        final Map<Speed, Bearing> runAngles = new HashMap<>();
        final Map<Speed, Speed> runVMGPredictionPerTrueWindSpeed = new HashMap<>();
        final Map<Speed, Duration> runAllowancePerTrueWindSpeed = new HashMap<>();
        final Map<Speed, Speed> windwardLeewardSpeedPredictionPerTrueWindSpeed = new HashMap<>();
        final Map<Speed, Speed> longDistanceSpeedPredictionPerTrueWindSpeed = new HashMap<>();
        final Map<Speed, Speed> circularRandomSpeedPredictionPerTrueWindSpeed = new HashMap<>();
        final Map<Speed, Speed> nonSpinnakerSpeedPredictionPerTrueWindSpeed = new HashMap<>();
        for (Speed tws : ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS) {
            String windSpeed = Integer.toString((int) tws.getKnots());
            String beatAngleKey = BEAT_ANGLE + windSpeed;
            String beatAllowanceKey = BEAT_ALLOWANCE + windSpeed;
            String runAngleKey = RUN_ANGLE + windSpeed;
            String runAllowanceKey = RUN_ALLOWANCE + windSpeed;
            String windwardLeewardKey = WINDWARD_LEEWARD + windSpeed;
            String longDistanceKey = LONG_DISTANCE + windSpeed;
            String circularRandomKey = CIRCULAR_RANDOM + windSpeed;
            String nonSpinnakerKey = NON_SPINNAKER + windSpeed;
            beatAngles.put(tws, new DegreeBearingImpl(Double.parseDouble(certificateValues.getValue(beatAngleKey))));
            beatAllowancePerTrueWindSpeed.put(tws, new SecondsDurationImpl(Double.parseDouble(certificateValues.getValue(beatAllowanceKey))));
            beatVMGPredictionPerTrueWindSpeed.put(tws, ORCCertificateImpl.NAUTICAL_MILE.inTime(beatAllowancePerTrueWindSpeed.get(tws)));
            runAngles.put(tws, new DegreeBearingImpl(Double.parseDouble(certificateValues.getValue(runAngleKey))));
            runAllowancePerTrueWindSpeed.put(tws, new SecondsDurationImpl(Double.parseDouble(certificateValues.getValue(runAllowanceKey))));
            runVMGPredictionPerTrueWindSpeed.put(tws, ORCCertificateImpl.NAUTICAL_MILE.inTime(runAllowancePerTrueWindSpeed.get(tws)));
            windwardLeewardSpeedPredictionPerTrueWindSpeed.put(tws, ORCCertificateImpl.NAUTICAL_MILE.inTime(
                    new SecondsDurationImpl(Double.parseDouble(certificateValues.getValue(windwardLeewardKey)))));
            longDistanceSpeedPredictionPerTrueWindSpeed.put(tws, ORCCertificateImpl.NAUTICAL_MILE.inTime(
                    new SecondsDurationImpl(Double.parseDouble(certificateValues.getValue(longDistanceKey)))));
            circularRandomSpeedPredictionPerTrueWindSpeed.put(tws, ORCCertificateImpl.NAUTICAL_MILE.inTime(
                    new SecondsDurationImpl(Double.parseDouble(certificateValues.getValue(circularRandomKey)))));
            nonSpinnakerSpeedPredictionPerTrueWindSpeed.put(tws, ORCCertificateImpl.NAUTICAL_MILE.inTime(
                    new SecondsDurationImpl(Double.parseDouble(certificateValues.getValue(nonSpinnakerKey)))));
            Map<Bearing, Speed> velocityPredictionPerTrueWindAngle = new HashMap<>();
            for (Bearing twa : ORCCertificateImpl.ALLOWANCES_TRUE_WIND_ANGLES ) {
                String twaCoursesKey = TWA_COURSES + Integer.toString((int) twa.getDegrees()) + windSpeed;
                velocityPredictionPerTrueWindAngle.put(twa, ORCCertificateImpl.NAUTICAL_MILE.inTime(
                        new SecondsDurationImpl(Double.parseDouble(certificateValues.getValue(twaCoursesKey)))));
            }
            velocityPredictionsPerTrueWindSpeedAndAngle.put(tws, velocityPredictionPerTrueWindAngle);
        }
        return new ORCCertificateImpl(certificateValues.getValue(NATCERTN_FILE_ID), searchString, boatName, boatclass, length,
                gph, cdl, velocityPredictionsPerTrueWindSpeedAndAngle,
                beatAngles, beatVMGPredictionPerTrueWindSpeed, beatAllowancePerTrueWindSpeed,
                runAngles, runVMGPredictionPerTrueWindSpeed,
                runAllowancePerTrueWindSpeed, windwardLeewardSpeedPredictionPerTrueWindSpeed,
                longDistanceSpeedPredictionPerTrueWindSpeed, circularRandomSpeedPredictionPerTrueWindSpeed, nonSpinnakerSpeedPredictionPerTrueWindSpeed);
    }

    @Override
    public Iterable<String> getSailNumbers() {
        return Collections.unmodifiableCollection(certificateValuesBySailnumber.keySet());
    }
}
