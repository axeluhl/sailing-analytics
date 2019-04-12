package com.sap.sailing.domain.ranking;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ORCCertificateImporterJSON implements ORCCertificateImporter {

    private Map<String, Object> data;
    private final int[] twsDistances = new int[]{6, 8, 10, 12, 14, 16, 20};

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
            result.put(((String) object.get("SailNo")).replaceAll(" ",""), object);
        }

        data = result;
    }

    @Override
    public ORCCertificate getCertificate(String sailnumber) {
        String searchString = sailnumber.replaceAll(" ", "").toUpperCase();
        System.out.println(searchString);

        JSONObject object = (JSONObject) data.get(searchString);
        System.out.println(object);

        Map<String, String> general = new HashMap<String, String>();
        Map<String, Number> hull    = new HashMap<String, Number>();
        Map<String, Number> sails   = new HashMap<String, Number>();
        Map<String, Number> scoring = new HashMap<String, Number>();
        Map<String, Map<Integer, Number>> twaCourses  = new HashMap<String, Map<Integer, Number>>();
        Map<String, Map<Integer, Number>> predefinedCourses  = new HashMap<String, Map<Integer, Number>>();

        for (Object key : object.keySet()) {
            switch ((String )key) {
                // TODO: nicer! Instead of switch cases, if else condition with ((String) key).matches(<regex>) expressions
                case "NatAuth": case "BIN": case "RefNo": case "SailNo": case "YachtName": case "Class": case "Builder":
                case "Designer": case "Division": case "IssueDate": {
                    general.put((String) key, (String) object.get(key));
                    break;
                }
                case "LOA": case "CrewWT": case "IMSL": case "Draft": case "MB": case "Dspl_Measurement": case "Stability_Index":
                case "Dynamic_Allowance": case "Age_Year": case "Dspl_Sailing": case "WSS": {
                    hull.put((String) key, (Number) object.get(key));
                    break;
                }
                case "Area_Main": case "Area_Jib": case "Area_Sym": case "Area_ASym": {
                    sails.put((String) key, (Number) object.get(key));
                    break;
                }
                case "GPH": case "TMF_Inshore": case "ILCWA": case "TMF_Offshore": case "OSN": case "CDL": case "TN_Offshore_Low": case "TN_Offshore_Medium": case "TN_Offshore_High": case "TN_Inshore_Low": case "TN_Inshore_Medium": case "TN_Inshore_High": case "TND_Offshore_Low": case "TND_Offshore_Medium": case "TND_Offshore_High": case "TND_Inshore_Low": case "TND_Inshore_Medium": case "TND_Inshore_High": case "Double_Handed_TOD": case "Double_Handed_TOT": case "OSN_Jibs": case "TMF_Jibs": {
                    scoring.put((String) key, (Number) object.get(key));
                    break;
                }
                case "Allowances": {
                    JSONObject allowances = (JSONObject) object.get("Allowances");
                    for (Object aKey : allowances.keySet()) {
                        System.out.println((String) aKey + allowances.get(aKey) + allowances.get(aKey).getClass());

                        Map<Integer, Number> twsMap = new HashMap<Integer, Number>();
                        JSONArray oneTwaArray = (JSONArray) allowances.get(aKey);
                        for (int i = 0; i < oneTwaArray.size(); i++) {
                            twsMap.put(twsDistances[i], (Number) oneTwaArray.get(i));
                        }

                        switch ((String) aKey) {
                            case "R52": case "R60": case "R75": case "R90": case "R110": case "R120": case "R135": case "R150": {
                                twaCourses.put((String) aKey, twsMap);
                            }
                            default: {
                                predefinedCourses.put((String) aKey, twsMap);
                            }
                        }
                    };
                }
                default: {}
            }
        }

        return new ORCCertificate(general, hull, sails, scoring, twaCourses, predefinedCourses);
    }

    @Override
    public Map<String, ORCCertificate> getCertificates(String[] sailnumbers) {
        Map<String, ORCCertificate> result = new HashMap<>();

        for(String sailnumber : sailnumbers) {
            result.put(sailnumber, getCertificate(sailnumber));
        }

        return result;
    }

    public Map<String, Object> getData() {
        return data;
    }

}