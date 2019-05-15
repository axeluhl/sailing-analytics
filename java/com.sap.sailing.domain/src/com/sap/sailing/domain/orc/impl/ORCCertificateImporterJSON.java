package com.sap.sailing.domain.orc.impl;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.orc.ORCCertificate;
import com.sap.sailing.domain.orc.ORCCertificateImporter;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.impl.DegreeBearingImpl;

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
    private static final int[] twsDistances = new int[] { 6, 8, 10, 12, 14, 16, 20 };
    private static final String RUN = "Run";
    private static final String BEAT = "Beat";

    /**
     * Receives an {@link InputStream} from different possible sources (web, local file, ...) and does parse the
     * {@code .json} file.
     * 
     * @param in
     * @throws IOException
     * @throws ParseException
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
        Map<Speed, Bearing> beatAngles = new HashMap<>();
        Map<Speed, Bearing> gybeAngles = new HashMap<>();
        Map<Speed, Map<Bearing, Duration>> timeAllowancesPerTrueWindSpeedAndAngle = new HashMap<>();
        Map<String, Map<Speed, Duration>> twaCourses        = new HashMap<>(); //TODO Rework, no String, directly parsed to the SAPSailing Semantics -> Bearing
        Map<String, Map<Speed, Duration>> predefinedCourses = new HashMap<>();
        String searchString = sailnumber.replaceAll(" ", "").toUpperCase();
        JSONObject object = (JSONObject) data.get(searchString);
        if (object == null) {
            return null;
        }
        for (Entry<Object, Object> entry : object.entrySet()) {
            switch ((String) entry.getKey()) {
                case "LOA":
                    length = new MeterDistance((double) entry.getValue());
                    break;
                case "Class":
                    boatclass = (String) entry.getValue();
                    break;
                case "GPH":    
                    gph = Duration.ONE_SECOND.times((double) entry.getValue());
                    break;
                case "Allowances":
                    JSONObject allowances = (JSONObject) object.get("Allowances");
                    for (Object aKey : allowances.keySet()) {
                        JSONArray array = (JSONArray) allowances.get(aKey);
    
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
                                    Duration.ONE_SECOND.times(((Number) array.get(i)).doubleValue()));
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
                                twaCourses.put((String) aKey, twsMap);
                                break;
                            case BEAT:
                            case RUN:
                                break;
                            default:
                                predefinedCourses.put((String) aKey, twsMap);
                            }
                    }
                    break;
                default:
                    break;
            }
        } 
        
        for (Speed tws : ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS) {
            timeAllowancesPerTrueWindSpeedAndAngle.put(tws, new HashMap<Bearing, Duration>());
        }
        for (String keyTWA : twaCourses.keySet()) {
            int twa = Integer.parseInt(keyTWA.substring(1));

            for (Speed keyTWS : twaCourses.get(keyTWA).keySet()) {
                timeAllowancesPerTrueWindSpeedAndAngle.get(keyTWS).put(new DegreeBearingImpl(twa), twaCourses.get(keyTWA).get(keyTWS));
            }
        }
        for (Speed tws : timeAllowancesPerTrueWindSpeedAndAngle.keySet()) {
            timeAllowancesPerTrueWindSpeedAndAngle.get(tws).put(beatAngles.get(tws), predefinedCourses.get(BEAT).get(tws));
            timeAllowancesPerTrueWindSpeedAndAngle.get(tws).put(gybeAngles.get(tws), predefinedCourses.get(RUN).get(tws));
        }
        
        
        return new ORCCertificateImpl(searchString, boatclass, length, gph, timeAllowancesPerTrueWindSpeedAndAngle, beatAngles, gybeAngles);
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
