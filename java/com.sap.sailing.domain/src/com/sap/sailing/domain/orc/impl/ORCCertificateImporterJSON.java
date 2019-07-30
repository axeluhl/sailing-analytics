package com.sap.sailing.domain.orc.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.common.orc.impl.ORCCertificateImpl;
import com.sap.sailing.domain.orc.ORCCertificateImporter;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.impl.DegreeBearingImpl;
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
public class ORCCertificateImporterJSON implements ORCCertificateImporter {

    private Map<String, Object> data;
    private static final String RUN = "Run";
    private static final String BEAT = "Beat";

    /**
     * Receives an {@link InputStream} from different possible sources (web, local file, ...) and does parse the
     * {@code .json} file.
     */
    public ORCCertificateImporterJSON(InputStream in) throws IOException, ParseException {
        Map<String, Object> result = new HashMap<>();
        String defaultEncoding = "UTF-8";
        BOMInputStream bOMInputStream = new BOMInputStream(in);
        ByteOrderMark bom = bOMInputStream.getBOM();
        String charsetName = bom == null ? defaultEncoding : bom.getCharsetName();
        InputStreamReader reader = new InputStreamReader(new BufferedInputStream(bOMInputStream), charsetName);
        JSONObject parsedJson = (JSONObject) new JSONParser().parse(reader);
        JSONArray dataArray = (JSONArray) parsedJson.get("rms");
        for (int i = 0; i < dataArray.size(); i++) {
            JSONObject object = (JSONObject) dataArray.get(i);
            result.put(((String) object.get("SailNo")).replaceAll(" ", "").toUpperCase(), object);
        }
        data = result;
    }

    /**
     * Returns an {@link ORCCertificateImpl} object for a given {@link String} key. If there is no map value for the
     * given key, the method returns {@link null}.
     */
    @Override
    public ORCCertificate getCertificate(String sailnumber) {
        String boatclass = null;
        Distance length  = null;
        Duration gph     = null;
        Double cdl       = null;
        Map<Speed, Bearing> beatAngles = new HashMap<>();
        Map<Speed, Bearing> gybeAngles = new HashMap<>();
        Map<Speed, Map<Bearing, Speed>> speedPredictionPerTrueWindSpeedAndAngle = new HashMap<>();
        Map<Bearing, Map<Speed, Duration>> allowanceDurationsPerTrueWindAngleAndSpeed = new HashMap<>();        //per nautical mile
        Map<String, Map<Speed, Duration>> predefinedAllowanceDurationsPerTrueWindSpeed = new HashMap<>();       //per nautical mile
        String searchString = sailnumber.replaceAll(" ", "").toUpperCase();
        JSONObject object = (JSONObject) data.get(searchString);
        if (object == null) {
            //TODO Throw Exception for sailnumber not found. InvalidArgumentException?
            return null;
        }
        for (Entry<Object, Object> entry : object.entrySet()) {
            switch ((String) entry.getKey()) {
                case "LOA":
                    length = new MeterDistance(((Number) entry.getValue()).doubleValue());
                    break;
                case "Class":
                    boatclass = (String) entry.getValue();
                    break;
                case "GPH":    
                    gph = Duration.ONE_SECOND.times(((Number) entry.getValue()).doubleValue());
                    break;
                case "CDL":
                    cdl = ((Number) entry.getValue()).doubleValue();
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
                                allowanceDurationsPerTrueWindAngleAndSpeed.put(new DegreeBearingImpl(Integer.parseInt(((String) aKey).substring(1))), twsMap);
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
            speedPredictionPerTrueWindSpeedAndAngle.put(tws, new HashMap<>());
        }
        for (Bearing keyTWA : allowanceDurationsPerTrueWindAngleAndSpeed.keySet()) {
            for (Speed keyTWS : allowanceDurationsPerTrueWindAngleAndSpeed.get(keyTWA).keySet()) {
                speedPredictionPerTrueWindSpeedAndAngle.get(keyTWS).put(keyTWA,
                        ORCCertificateImpl.NAUTICAL_MILE.inTime(allowanceDurationsPerTrueWindAngleAndSpeed.get(keyTWA).get(keyTWS)));
            }
        }
        final Map<Speed, Speed> beatVMGPredictionPerTrueWindSpeed = new HashMap<>();
        final Map<Speed, Duration> beatAllowancePerTrueWindSpeed = new HashMap<>();
        final Map<Speed, Speed> runVMGPredictionPerTrueWindSpeed = new HashMap<>();
        final Map<Speed, Duration> runAllowancePerTrueWindSpeed = new HashMap<>();
        for (final Speed tws : speedPredictionPerTrueWindSpeedAndAngle.keySet()) {
            beatVMGPredictionPerTrueWindSpeed.put(tws, ORCCertificateImpl.NAUTICAL_MILE.inTime(predefinedAllowanceDurationsPerTrueWindSpeed.get(BEAT).get(tws)));
            beatAllowancePerTrueWindSpeed.put(tws, predefinedAllowanceDurationsPerTrueWindSpeed.get(BEAT).get(tws));
            runVMGPredictionPerTrueWindSpeed.put(tws, ORCCertificateImpl.NAUTICAL_MILE.inTime(predefinedAllowanceDurationsPerTrueWindSpeed.get(RUN).get(tws)));
            runAllowancePerTrueWindSpeed.put(tws, predefinedAllowanceDurationsPerTrueWindSpeed.get(RUN).get(tws));
        }
        return new ORCCertificateImpl(searchString, boatclass, length, gph, cdl, speedPredictionPerTrueWindSpeedAndAngle, beatAngles, beatVMGPredictionPerTrueWindSpeed, beatAllowancePerTrueWindSpeed, gybeAngles, runVMGPredictionPerTrueWindSpeed, runAllowancePerTrueWindSpeed);
    }

    /**
     * Returns a {@link Map} of {@link ORCCertificateImpl} keyed by the {@link String} sailnumbers, which were given as
     * an input inside an array.
     */
    @Override
    public Map<String, ORCCertificate> getCertificates(String[] sailnumbers) {
        Map<String, ORCCertificate> result = new HashMap<>();

        for (String sailnumber : sailnumbers) {
            result.put(sailnumber, getCertificate(sailnumber));
        }

        return result;
    }
}
