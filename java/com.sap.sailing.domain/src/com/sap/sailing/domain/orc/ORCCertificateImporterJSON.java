package com.sap.sailing.domain.orc;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.impl.DegreeBearingImpl;


/**
 * Represents a file in format {@code .json} which is a simple ASCII file format. The {@code .json} file contains an array of maps,
 * each map representing one measurement certificate. The maps have the specific measurement values accessible with a 
 * {@link String} for each boat.
 * <p>
 * 
 * The result of successfully parsing a {@code .json} file is a map keyed by the sailing number,
 * with values being equal-sized maps from the key names to the {@link String} values.
 * 
 * @author Daniel Lisunkin (i505543)
 *
 */
public class ORCCertificateImporterJSON implements ORCCertificateImporter {

    private Map<String, Object> data;
    private final int[] twsDistances = new int[] { 6, 8, 10, 12, 14, 16, 20 };

    /**
     * Receives an InputStream from different possible sources (web, local file, ...) and does parse the {@code .json} file.
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
            result.put(((String) object.get("SailNo")).replaceAll(" ", ""), object);
        }

        data = result;
    }

    /**
     * Returns an ORCCertificate object for a given {@link String} key. If there is no map value for the given key, the method returns {@link null}.
     */
    @Override
    public ORCCertificate getCertificate(String sailnumber) {

        String searchString = sailnumber.replaceAll(" ", "").toUpperCase();

        JSONObject object = (JSONObject) data.get(searchString);
        if (object == null) {
            return null;
        }

        Map<String, String> general = new HashMap<>();
        Map<String, Number> hull = new HashMap<>();
        Map<String, Number> sails = new HashMap<>();
        Map<String, Number> scoring = new HashMap<>();
        Map<String, Map<Speed, Duration>> twaCourses = new HashMap<>();
        Map<String, Map<Speed, Duration>> predefinedCourses = new HashMap<>();
        Map<Speed, Bearing> beatAngles = new HashMap<>();
        Map<Speed, Bearing> gybeAngles = new HashMap<>();

        for (Object key : object.keySet()) {
            switch ((String) key) {
            // TODO: nicer! Instead of switch cases, if else condition with ((String) key).matches(<regex>) expressions
            case "NatAuth":
            case "BIN":
            case "RefNo":
            case "SailNo":
            case "YachtName":
            case "Class":
            case "Builder":
            case "Designer":
            case "Division":
            case "IssueDate": {
                general.put((String) key, (String) object.get(key));
                break;
            }
            case "LOA":
            case "CrewWT":
            case "IMSL":
            case "Draft":
            case "MB":
            case "Dspl_Measurement":
            case "Stability_Index":
            case "Dynamic_Allowance":
            case "Age_Year":
            case "Dspl_Sailing":
            case "WSS": {
                hull.put((String) key, (Number) object.get(key));
                break;
            }
            case "Area_Main":
            case "Area_Jib":
            case "Area_Sym":
            case "Area_ASym": {
                sails.put((String) key, (Number) object.get(key));
                break;
            }
            case "GPH":
            case "TMF_Inshore":
            case "ILCWA":
            case "TMF_Offshore":
            case "OSN":
            case "CDL":
            case "TN_Offshore_Low":
            case "TN_Offshore_Medium":
            case "TN_Offshore_High":
            case "TN_Inshore_Low":
            case "TN_Inshore_Medium":
            case "TN_Inshore_High":
            case "TND_Offshore_Low":
            case "TND_Offshore_Medium":
            case "TND_Offshore_High":
            case "TND_Inshore_Low":
            case "TND_Inshore_Medium":
            case "TND_Inshore_High":
            case "Double_Handed_TOD":
            case "Double_Handed_TOT":
            case "OSN_Jibs":
            case "TMF_Jibs": {
                scoring.put((String) key, (Number) object.get(key));
                break;
            }
            case "Allowances": {
                JSONObject allowances = (JSONObject) object.get("Allowances");
                for (Object aKey : allowances.keySet()) {
                    JSONArray array = (JSONArray) allowances.get(aKey);

                    if (((String) aKey).equals("BeatAngle")) {
                        for (int i = 0; i < array.size(); i++) {
                            beatAngles.put(new KnotSpeedImpl(twsDistances[i]),
                                    new DegreeBearingImpl((double) array.get(i)));
                        }
                        break;
                    } else if (((String) aKey).equals("GybeAngle")) {
                        for (int i = 0; i < array.size(); i++) {
                            gybeAngles.put(new KnotSpeedImpl(twsDistances[i]),
                                    new DegreeBearingImpl((double) array.get(i)));
                        }
                        break;
                    }

                    Map<Speed, Duration> twsMap = new HashMap<>();
                    for (int i = 0; i < array.size(); i++) {
                        twsMap.put(new KnotSpeedImpl(twsDistances[i]),
                                Duration.ONE_SECOND.times((double) array.get(i)));
                    }

                    switch ((String) aKey) {
                    case "R52":
                    case "R60":
                    case "R75":
                    case "R90":
                    case "R110":
                    case "R120":
                    case "R135":
                    case "R150": {
                        twaCourses.put((String) aKey, twsMap);
                        break;
                    }
                    default: {
                        predefinedCourses.put((String) aKey, twsMap);
                    }
                    }
                }
                break;
            }
            default: {
            }
            }
        }

        return new ORCCertificate(general, hull, sails, scoring, twaCourses, predefinedCourses, beatAngles, gybeAngles);
    }

    @Override
    public Map<String, ORCCertificate> getCertificates(String[] sailnumbers) {
        Map<String, ORCCertificate> result = new HashMap<>();

        for (String sailnumber : sailnumbers) {
            result.put(sailnumber, getCertificate(sailnumber));
        }

        return result;
    }

    public Map<String, Object> getData() {
        return data;
    }

}