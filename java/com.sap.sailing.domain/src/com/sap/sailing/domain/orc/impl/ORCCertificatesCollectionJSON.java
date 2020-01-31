package com.sap.sailing.domain.orc.impl;

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
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
    private static final String GYBE_ANGLE = "GybeAngle";
    private static final String BEAT_ANGLE = "BeatAngle";
    private static final String ALLOWANCES = "Allowances";
    private static final String ISSUE_DATE = "IssueDate";
    private static final String CDL2 = "CDL";
    private static final String GPH2 = "GPH";
    private static final String CLASS = "Class";
    private static final String YACHT_NAME = "YachtName";
    private static final String LOA = "LOA";
    private static final String SAIL_NO = "SailNo";
    private static final String CERT_NO = "CertNo";
    private static final String BIN = "BIN";
    private static final String NAT_AUTH = "NatAuth";
    private static final String RUN = "Run";
    private static final String BEAT = "Beat";
    private static final String WINDWARD_LEEWARD = "WL";
    private static final String LONG_DISTANCE = "OC";
    private static final String CIRCULAR_RANDOM = "CR";
    private static final String NON_SPINNAKER = "NS";
    private static final String EMPTY_CERT_NO = "      ";

    private Map<String, JSONObject> certificateJsonObjectsByCertificateId;

    /**
     * Receives an {@link InputStream} from different possible sources (web, local file, ...) and does parse the
     * {@code .json} file.
     */
    public ORCCertificatesCollectionJSON(Iterable<JSONObject> data) {
        this.certificateJsonObjectsByCertificateId = new HashMap<>();
        for (final JSONObject o : data) {
            if (this.certificateJsonObjectsByCertificateId.put(getId(o), o) != null) {
                throw new IllegalArgumentException("Certificate ID in collection not unique: " + getId(o));
            }
        }
    }

    /**
     * Returns an {@link ORCCertificateImpl} object for a given {@link String} key. If there is no map value for the
     * given key, the method returns {@link null}.
     */
    @Override
    public ORCCertificate getCertificateById(String certificateId) {
        String boatName = null;
        String boatclass = null;
        Distance length = null;
        Duration gph = null;
        Double cdl = null;
        TimePoint issueDate = null;
        Map<Speed, Bearing> beatAngles = new HashMap<>();
        Map<Speed, Bearing> gybeAngles = new HashMap<>();
        Map<Speed, Map<Bearing, Speed>> velocityPredictionPerTrueWindSpeedAndAngle = new HashMap<>();
        Map<Bearing, Map<Speed, Duration>> allowanceDurationsPerTrueWindAngleAndSpeed = new HashMap<>();  // per
                                                                                                          // nautical
                                                                                                          // mile
        Map<String, Map<Speed, Duration>> predefinedAllowanceDurationsPerTrueWindSpeed = new HashMap<>(); // per
                                                                                                          // nautical
                                                                                                          // mile
        boolean isNewFormatTWA = false; // determine whether use default TWS and TWA values or custom TWS and TWA values
        Map<Double, Speed> trueWindSpeedMap = new TreeMap<Double,Speed>();// Using TreeMap implementation to save the TrueWindSpeed
                                                                          // So that it first sort the entries as well as no duplicates
        Map<Double, Bearing> trueWindAngleMap = new TreeMap<Double,Bearing>();// Same like trueWindSpeedMap
        Pattern p = Pattern.compile("^([R][0-9]{1,9}[.]{0,1}[0-9]{1})$");       //Pattern to recognize the new pattern
        JSONObject object = certificateJsonObjectsByCertificateId.get(certificateId);
        if (object == null) {
            // TODO Throw Exception for certificate by id not found. InvalidArgumentException?
            return null;
        }
        String sailNumber = null;
        for (Entry<Object, Object> entry : object.entrySet()) {
            switch ((String) entry.getKey()) {
            case SAIL_NO:
                sailNumber = entry.getValue() == null ? null : entry.getValue().toString();
                break;
            case LOA:
                length = new MeterDistance(((Number) entry.getValue()).doubleValue());
                break;
            case YACHT_NAME:
                boatName = entry.getValue() == null ? null : entry.getValue().toString();
                break;
            case CLASS:
                boatclass = (String) entry.getValue();
                break;
            case GPH2:
                gph = new SecondsDurationImpl(((Number) entry.getValue()).doubleValue());
                break;
            case CDL2:
                cdl = ((Number) entry.getValue()).doubleValue();
                break;
            case ISSUE_DATE:
                Date date = DatatypeConverter.parseDateTime((String) entry.getValue()).getTime();
                issueDate = new MillisecondsTimePoint(date);
                break;
            case ALLOWANCES:
                final JSONObject allowances = (JSONObject) object.get(ALLOWANCES);
                
               // Before going further into parsing Allowances, First need to check whether 
               // The bins are with old format or New, If New Format is given then need to parse
               // and calculate the bins accordingly
                for (final Object aKey : allowances.keySet()) {
                    String keyString = (String) aKey;
                    boolean found = p.matcher(keyString).lookingAt();
                    if (found) {
                        final JSONArray array = (JSONArray) allowances.get(keyString);
                        if(array.size()<1 || ! (array.get(0) instanceof JSONObject)) {
                            break;
                        }
                        isNewFormatTWA = true;
                        Map<Speed, Duration> twsMap = new HashMap<>(); // This map is use to save all the True wind speed and allowances per nautical miles against the True wind angle provide with new format.
                       
                        for (int i = 0; i < array.size(); i++) {
                            JSONObject twa = (JSONObject) array.get(i);
                            Double twsValue = (Double) twa.get("twsInKts");
                            trueWindSpeedMap.put(twsValue,new KnotSpeedImpl(twsValue));
                            twsMap.put(trueWindSpeedMap.get(twsValue),
                                    new SecondsDurationImpl((Double) twa.get("allInSPNM")));
                        }
                        Double trueWindAngleValue = Double.parseDouble((keyString).substring(1));
                        trueWindAngleMap.put(trueWindAngleValue,new DegreeBearingImpl(trueWindAngleValue));
                        allowanceDurationsPerTrueWindAngleAndSpeed
                                .put(trueWindAngleMap.get(trueWindAngleValue), twsMap);
                    }
                }
                //Finding out the new bins end here
                
                final Double [] trueWindSpeedArray = new Double[trueWindSpeedMap.size()];
                trueWindSpeedMap.keySet().toArray(trueWindSpeedArray);
                for (final Object aKey : allowances.keySet()) {
                    final JSONArray array = (JSONArray) allowances.get(aKey);
                    if (((String) aKey).equals(BEAT_ANGLE)) {
                        for (int i = 0; i < array.size(); i++) {
                            beatAngles.put(isNewFormatTWA?new KnotSpeedImpl(trueWindSpeedArray[i]):ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[i],
                                    new DegreeBearingImpl(((Number) array.get(i)).doubleValue()));
                        }
                        continue;
                    }
                    if (((String) aKey).equals(GYBE_ANGLE)) {
                        for (int i = 0; i < array.size(); i++) {
                            gybeAngles.put(isNewFormatTWA?new KnotSpeedImpl(trueWindSpeedArray[i]):ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[i],
                                    new DegreeBearingImpl(((Number) array.get(i)).doubleValue()));
                        }
                        continue;
                    }
                    Map<Speed, Duration> twsMap = new HashMap<>();
                    // As we don't want to calculate the allowancesDuarationPerTrueWindAngleAndSpeed for new format TWS and TWA
                    // So we need to ignore that parsing. If the TWS and TWA are with old format then this calculation occure's here
                    if(!isNewFormatTWA){
                        for (int i = 0; i < array.size(); i++) {
                            twsMap.put(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[i],
                                    new SecondsDurationImpl(((Number) array.get(i)).doubleValue()));
                        }
                    } 
                    if(isNewFormatTWA && !p.matcher((String) aKey).lookingAt()) {
                        for (int i = 0; i < array.size(); i++) {
                            twsMap.put(isNewFormatTWA?new KnotSpeedImpl(trueWindSpeedArray[i]):ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[i],
                                    new SecondsDurationImpl(((Number) array.get(i)).doubleValue()));
                        }
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
                            allowanceDurationsPerTrueWindAngleAndSpeed
                                    .put(new DegreeBearingImpl(Integer.parseInt(((String) aKey).substring(1))), twsMap);
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
        if(isNewFormatTWA) {
            trueWindSpeedMap.keySet().forEach(tws->{
                velocityPredictionPerTrueWindSpeedAndAngle.put(trueWindSpeedMap.get(tws), new HashMap<>());
            });
           
        }else {
            for (Speed tws : ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS) {
                velocityPredictionPerTrueWindSpeedAndAngle.put(tws, new HashMap<>());
            }
        }
        for (Bearing keyTWA : allowanceDurationsPerTrueWindAngleAndSpeed.keySet()) {
            for (Speed keyTWS : allowanceDurationsPerTrueWindAngleAndSpeed.get(keyTWA).keySet()) {
                velocityPredictionPerTrueWindSpeedAndAngle.get(keyTWS).put(keyTWA, ORCCertificateImpl.NAUTICAL_MILE
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
        
        // Need to populate the ORCCertificateImpl with custom TWA and TWS that's why there are two constructor's are being used.
        if(isNewFormatTWA) {
            Bearing [] dynamicAllowancesTrueWindAngles = new Bearing[trueWindAngleMap.values().size()];
            Speed [] dynamicAllowancesTrueWindSpeeds = new Speed[trueWindSpeedMap.values().size()];
            trueWindAngleMap.values().toArray(dynamicAllowancesTrueWindAngles);
            trueWindSpeedMap.values().toArray(dynamicAllowancesTrueWindSpeeds);
           
            return new ORCCertificateImpl(dynamicAllowancesTrueWindSpeeds,dynamicAllowancesTrueWindAngles,getId(object), sailNumber, boatName, boatclass, length, gph, cdl, issueDate,
                    velocityPredictionPerTrueWindSpeedAndAngle, beatAngles, beatVMGPredictionPerTrueWindSpeed,
                    beatAllowancePerTrueWindSpeed, gybeAngles, runVMGPredictionPerTrueWindSpeed,
                    runAllowancePerTrueWindSpeed, windwardLeewardSpeedPredictionPerTrueWindSpeed,
                    longDistanceSpeedPredictionPerTrueWindSpeed, circularRandomSpeedPredictionPerTrueWindSpeed,
                    nonSpinnakerSpeedPredictionPerTrueWindSpeed);
        }else {
        return new ORCCertificateImpl(getId(object), sailNumber, boatName, boatclass, length, gph, cdl, issueDate,
                velocityPredictionPerTrueWindSpeedAndAngle, beatAngles, beatVMGPredictionPerTrueWindSpeed,
                beatAllowancePerTrueWindSpeed, gybeAngles, runVMGPredictionPerTrueWindSpeed,
                runAllowancePerTrueWindSpeed, windwardLeewardSpeedPredictionPerTrueWindSpeed,
                longDistanceSpeedPredictionPerTrueWindSpeed, circularRandomSpeedPredictionPerTrueWindSpeed,
                nonSpinnakerSpeedPredictionPerTrueWindSpeed);
        }
    }


    private String getId(JSONObject certificateAsJson) {
        return getIdFromFields(certificateAsJson.get(NAT_AUTH).toString(),
                certificateAsJson.get(CERT_NO) == null ? EMPTY_CERT_NO : certificateAsJson.get(CERT_NO).toString(),
                certificateAsJson.get(BIN).toString());
    }

    private String getIdFromFields(final String natAuth, final String certNo, final String bin) {
        return natAuth + certNo + bin;
    }

    @Override
    public Iterable<String> getCertificateIds() {
        return Collections.unmodifiableCollection(certificateJsonObjectsByCertificateId.keySet());
    }
}
