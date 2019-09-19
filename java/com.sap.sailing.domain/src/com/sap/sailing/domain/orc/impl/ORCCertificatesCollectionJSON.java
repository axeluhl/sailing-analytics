package com.sap.sailing.domain.orc.impl;

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.common.orc.impl.ORCCertificateImpl;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.SecondsDurationImpl;

/**
 * Represents a file in format {@code .json} which is a simple ASCII file format. The {@code .json} file contains an
 * array of maps, each map representing one measurement certificate. The maps have the specific measurement values
 * accessible with a {@link String} for each boat.
 * <p>
 * 
 * The result of successfully parsing a {@code .json} file is a map keyed by the sailing number, with values being
 * equal-sized maps from the key names to the {@link String} values.
 * 
 * @author Daniel Lisunkin (i505543)
 *
 */
public class ORCCertificatesCollectionJSON extends AbstractORCCertificatesCollection {
    private Map<String, JSONObject> data;
    private static final String RUN = "Run";
    private static final String BEAT = "Beat";
    private static final String WINDWARD_LEEWARD = "WL";
    private static final String LONG_DISTANCE = "OC";
    private static final String CIRCULAR_RANDOM = "CR";
    private static final String NON_SPINNAKER = "NS";

    /**
     * Receives an {@link InputStream} from different possible sources (web, local file, ...) and does parse the
     * {@code .json} file.
     */
    public ORCCertificatesCollectionJSON(Map<String, JSONObject> data) {
        this.data = new HashMap<>();
        for (final Entry<String, JSONObject> e : data.entrySet()) {
            if (this.data.put(getCanonicalizedSailNumber(e.getKey()), e.getValue()) != null) {
                throw new IllegalArgumentException(
                        "Certificate sail numbers in collection not unique after canonicalization: " + e.getKey()
                                + " is canonicalized to " + getCanonicalizedSailNumber(e.getKey())
                                + " for which a certificate is already in the collection.");
            }
        }
    }

    /**
     * Returns an {@link ORCCertificateImpl} object for a given {@link String} key. If there is no map value for the
     * given key, the method returns {@link null}.
     */
    @Override
    public ORCCertificate getCertificateBySailNumber(String sailnumber) {
        String boatName  = null;
        String boatclass = null;
        Distance length  = null;
        Duration gph     = null;
        Double cdl       = null;
        TimePoint issueDate = null;
        Map<Speed, Bearing> beatAngles = new HashMap<>();
        Map<Speed, Bearing> gybeAngles = new HashMap<>();
        Map<Speed, Map<Bearing, Speed>> velocityPredictionPerTrueWindSpeedAndAngle = new HashMap<>();
        Map<Bearing, Map<Speed, Duration>> allowanceDurationsPerTrueWindAngleAndSpeed = new HashMap<>();        //per nautical mile
        Map<String, Map<Speed, Duration>> predefinedAllowanceDurationsPerTrueWindSpeed = new HashMap<>();       //per nautical mile
        String searchString = getCanonicalizedSailNumber(sailnumber);
        JSONObject object = data.get(searchString);
        if (object == null) {
            //TODO Throw Exception for sailnumber not found. InvalidArgumentException?
            return null;
        }
        String natAuth = null;
        String bin = null;
        String certNo = null;
        for (Entry<Object, Object> entry : object.entrySet()) {
            switch ((String) entry.getKey()) {
                case "NatAuth":
                    natAuth = entry.getValue().toString();
                    break;
                case "BIN":
                    bin = entry.getValue().toString();
                    break;
                case "CertNo":
                    certNo = entry.getValue().toString();
                    break;
                case "LOA":
                    length = new MeterDistance(((Number) entry.getValue()).doubleValue());
                    break;
                case "YachtName":
                    boatName = entry.getValue().toString();
                    break;
                case "Class":
                    boatclass = (String) entry.getValue();
                    break;
                case "GPH":    
                    gph = new SecondsDurationImpl(((Number) entry.getValue()).doubleValue());
                    break;
                case "CDL":
                    cdl = ((Number) entry.getValue()).doubleValue();
                case "IssueDate":
                    Date date = DatatypeConverter.parseDateTime((String) entry.getValue()).getTime();
                    issueDate = new MillisecondsTimePoint(date);
                case "Allowances":
                    final JSONObject allowances = (JSONObject) object.get("Allowances");
                    for (final Object aKey : allowances.keySet()) {
                        final JSONArray array = (JSONArray) allowances.get(aKey);
                        if (((String) aKey).equals("BeatAngle")) {
                            for (int i = 0; i < array.size(); i++) {
                                beatAngles.put(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[i],
                                        new DegreeBearingImpl(((Number) array.get(i)).doubleValue()));
                            }
                            continue;
                        }
                        if (((String) aKey).equals("GybeAngle")) {
                            for (int i = 0; i < array.size(); i++) {
                                gybeAngles.put(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[i],
                                        new DegreeBearingImpl(((Number) array.get(i)).doubleValue()));
                            }
                            continue;
                        }
                        Map<Speed, Duration> twsMap = new HashMap<>();
                        for (int i = 0; i < array.size(); i++) {
                            twsMap.put(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[i],
                                    new SecondsDurationImpl(((Number) array.get(i)).doubleValue()));
                        }
                        switch ((String) aKey) {
                            case "R52":
                            case "R60":
                            case "R75":
                            case "R90":
                            case "R110":
                            case "R120":
                            case "R135":
                            case "R150":
                                allowanceDurationsPerTrueWindAngleAndSpeed.put(
                                        new DegreeBearingImpl(Integer.parseInt(((String) aKey).substring(1))), twsMap);
                                break;
                            case BEAT:
                            case RUN:
                                predefinedAllowanceDurationsPerTrueWindSpeed.put((String) aKey, twsMap);
                                break;
                            default:
                                predefinedAllowanceDurationsPerTrueWindSpeed.put((String) aKey, twsMap);
                                break;
                            }
                    }
                    break;
                default:
                    break;
            }
        } 
        for (Speed tws : ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS) {
            velocityPredictionPerTrueWindSpeedAndAngle.put(tws, new HashMap<>());
        }
        for (Bearing keyTWA : allowanceDurationsPerTrueWindAngleAndSpeed.keySet()) {
            for (Speed keyTWS : allowanceDurationsPerTrueWindAngleAndSpeed.get(keyTWA).keySet()) {
                velocityPredictionPerTrueWindSpeedAndAngle.get(keyTWS).put(keyTWA,
                        ORCCertificateImpl.NAUTICAL_MILE
                        .inTime(allowanceDurationsPerTrueWindAngleAndSpeed.get(keyTWA).get(keyTWS)));
            }
        }
        final Map<Speed, Speed> beatVMGPredictionPerTrueWindSpeed = new HashMap<>();
        final Map<Speed, Duration> beatAllowancePerTrueWindSpeed = new HashMap<>();
        final Map<Speed, Speed> runVMGPredictionPerTrueWindSpeed = new HashMap<>();
        final Map<Speed, Duration> runAllowancePerTrueWindSpeed = new HashMap<>();
        final Map<Speed, Speed> windwardLeewardSpeedPredictionPerTrueWindSpeed = new HashMap<>();
        final Map<Speed, Speed> longDistanceSpeedPredictionPerTrueWindSpeed = new HashMap<>();
        final Map<Speed, Speed> circularRandomSpeedPredictionPerTrueWindSpeed = new HashMap<>();
        final Map<Speed, Speed> nonSpinnakerSpeedPredictionPerTrueWindSpeed = new HashMap<>();
        for (final Speed tws : velocityPredictionPerTrueWindSpeedAndAngle.keySet()) {
            beatVMGPredictionPerTrueWindSpeed.put(tws, ORCCertificateImpl.NAUTICAL_MILE
                    .inTime(predefinedAllowanceDurationsPerTrueWindSpeed.get(BEAT).get(tws)));
            beatAllowancePerTrueWindSpeed.put(tws, predefinedAllowanceDurationsPerTrueWindSpeed.get(BEAT).get(tws));
            runVMGPredictionPerTrueWindSpeed.put(tws, ORCCertificateImpl.NAUTICAL_MILE
                    .inTime(predefinedAllowanceDurationsPerTrueWindSpeed.get(RUN).get(tws)));
            runAllowancePerTrueWindSpeed.put(tws, predefinedAllowanceDurationsPerTrueWindSpeed.get(RUN).get(tws));
            windwardLeewardSpeedPredictionPerTrueWindSpeed.put(tws, ORCCertificateImpl.NAUTICAL_MILE
                    .inTime(predefinedAllowanceDurationsPerTrueWindSpeed.get(WINDWARD_LEEWARD).get(tws)));
            longDistanceSpeedPredictionPerTrueWindSpeed.put(tws, ORCCertificateImpl.NAUTICAL_MILE
                    .inTime(predefinedAllowanceDurationsPerTrueWindSpeed.get(LONG_DISTANCE).get(tws)));
            circularRandomSpeedPredictionPerTrueWindSpeed.put(tws, ORCCertificateImpl.NAUTICAL_MILE
                    .inTime(predefinedAllowanceDurationsPerTrueWindSpeed.get(CIRCULAR_RANDOM).get(tws)));
            nonSpinnakerSpeedPredictionPerTrueWindSpeed.put(tws, ORCCertificateImpl.NAUTICAL_MILE
                    .inTime(predefinedAllowanceDurationsPerTrueWindSpeed.get(NON_SPINNAKER).get(tws)));
        }
        return new ORCCertificateImpl(natAuth + certNo + bin, searchString, boatName, boatclass, length, gph, cdl,
                issueDate, velocityPredictionPerTrueWindSpeedAndAngle, beatAngles, beatVMGPredictionPerTrueWindSpeed,
                beatAllowancePerTrueWindSpeed, gybeAngles, runVMGPredictionPerTrueWindSpeed,
                runAllowancePerTrueWindSpeed, windwardLeewardSpeedPredictionPerTrueWindSpeed,
                longDistanceSpeedPredictionPerTrueWindSpeed, circularRandomSpeedPredictionPerTrueWindSpeed,
                nonSpinnakerSpeedPredictionPerTrueWindSpeed);
    }

    @Override
    public Iterable<String> getSailNumbers() {
        return Collections.unmodifiableCollection(data.keySet());
    }
}
