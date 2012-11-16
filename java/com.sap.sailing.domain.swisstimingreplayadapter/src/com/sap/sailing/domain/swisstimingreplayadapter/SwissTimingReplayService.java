package com.sap.sailing.domain.swisstimingreplayadapter;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class SwissTimingReplayService {
    
    private static final int EOT = 0x04;
    public static final DateFormat SWISSTIMING_DATEFORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public static List<SwissTimingReplayRace> listReplayRaces(String swissTimingUrlText) {
        URL url;
        try {
            url = new URL(swissTimingUrlText);
            return parseJSONObject(url.openStream(), swissTimingUrlText);
        } catch (Exception e) { //MalformedURLException | IOException | ParseException | org.json.simple.parser.ParseException)
            throw new RuntimeException(e);
        }
    }

    static List<SwissTimingReplayRace>  parseJSONObject(InputStream inputStream, String swissTimingUrlText) throws IOException, ParseException, org.json.simple.parser.ParseException {
        JSONArray json = (JSONArray) new JSONParser().parse(new InputStreamReader(inputStream));
        List<SwissTimingReplayRace> result = new ArrayList<SwissTimingReplayRace>();
        for (Object raceEntry : json) {
            JSONObject jsonRaceEntry = (JSONObject) raceEntry;
            String startTimeText = (String) jsonRaceEntry.get("start");
            Date startTime = startTimeText == null ? null : SWISSTIMING_DATEFORMAT.parse(startTimeText);
            SwissTimingReplayRace replayRace = new SwissTimingReplayRace(
                    swissTimingUrlText, 
                    (String) jsonRaceEntry.get("flight_number"),
                    (String) jsonRaceEntry.get("race_id"),
                    (String) jsonRaceEntry.get("rsc"),
                    (String) jsonRaceEntry.get("name"),
                    (String) jsonRaceEntry.get("class"),
                    startTime ,
                    (String) jsonRaceEntry.get("link"));
            result.add(replayRace);
        }
        return result;
    }

    public static void loadRaceData(String link) {
        URL raceDataUrl;
        try {
            raceDataUrl = new URL("http://" + link);
            InputStream urlInputStream = raceDataUrl.openStream();
            readData(urlInputStream);
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }

    static void readData(InputStream urlInputStream) throws IOException {
        DataInputStream data = new DataInputStream(urlInputStream);
        byte delim = data.readByte();
        while (delim != EOT) {
            
            byte messageIdentificationCode = data.readByte();
            short length = data.readShort();
            System.out.println("length: " + length);
            switch (messageIdentificationCode) {
            case MessageIdentificationCodes.MIC_00_Reference_Timestamp:
                System.out.println("MIC_00_Reference_Timestamp: " + data.readLong());
                break;
            case MessageIdentificationCodes.MIC_01_Reference_Location:
                System.out.println("MIC_01_Reference_Location: " + data.readLong());
                break;
            case MessageIdentificationCodes.MIC_02_Protocol_Version:
                System.out.println("MIC_02_Protocol_Version: " + data.readShort());
                break;
            case MessageIdentificationCodes.MIC_03_Reserved:
            case MessageIdentificationCodes.MIC_04_Reserved:
            case MessageIdentificationCodes.MIC_05_Reserved:
            case MessageIdentificationCodes.MIC_06_Reserved:
            case MessageIdentificationCodes.MIC_07_Reserved:
            case MessageIdentificationCodes.MIC_08_Reserved:
                System.out.println("Reserved: " + messageIdentificationCode);
                break;
            case MessageIdentificationCodes.MIC_09_Keyframe_Index:
                System.out.println("MIC_09_Keyframe_Index: " + data.readInt());
                System.out.println("Position: " + data.readInt());
                break;
            case MessageIdentificationCodes.MIC_10_RSC_CID:
                byte[] chars = new byte[9]; 
                data.read(chars);
                System.out.println("MIC_10_RSC_CID: " + new String(chars));
                break;
            case MessageIdentificationCodes.MIC_11_Competitors:
                System.out.println("MIC_11_Competitors:");
                chars = new byte[3]; 
                data.read(chars);
                System.out.println("Nation: " + new String(chars));
                short identifierLength = data.readShort();
                System.out.println("Identifier length: " + identifierLength);
                byte[] identifier = new byte[identifierLength];
                data.read(identifier);
                System.out.println("Identifier length: " + new String(identifier));
                break;
            default:
                System.out.println("Unknown: " + messageIdentificationCode);
                break;
            }
            
            delim = data.readByte();
        }
    }

}
