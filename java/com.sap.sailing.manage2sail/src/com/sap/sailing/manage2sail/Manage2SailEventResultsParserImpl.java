package com.sap.sailing.manage2sail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.CompetitorGenderType;
import com.sap.sailing.util.DateParser;
import com.sap.sailing.util.InvalidDateException;

public class Manage2SailEventResultsParserImpl implements Manage2SailEventResultsParser {

    /**
     * @param is closed before the method returns, also in case of exception
     * @throws IOException 
     */
    public EventResultDescriptor getEventResult(InputStream is) throws IOException {
        EventResultDescriptor result = null;
        try {
            JSONObject jsonRoot = (JSONObject) new JSONParser().parse(new InputStreamReader(is, "UTF-8"));
            result = new EventResultDescriptor();
            result.setId((String) jsonRoot.get("Id"));
            result.setIsafId((String) jsonRoot.get("IsafId"));
            result.setName((String) jsonRoot.get("Name"));
            result.setXrrUrl(parseURL(jsonRoot, "XrrUrl"));
            result.setTrackingDataHost((String) jsonRoot.get("TrackingDataHost"));
            result.setTrackingDataPort(parseInteger(jsonRoot, "TrackingDataPort"));
            
            JSONArray jsonRegattas = (JSONArray) jsonRoot.get("Regattas");
            for (Object regattaObject: jsonRegattas) {
                RegattaResultDescriptor regattaResult = new RegattaResultDescriptor(); 
                JSONObject jsonRegatta = (JSONObject) regattaObject;
                regattaResult.setId((String) jsonRegatta.get("Id"));
                regattaResult.setIsafId((String) jsonRegatta.get("IsafId"));
                regattaResult.setExternalId((String) jsonRegatta.get("ExternalId"));
                regattaResult.setName((String) jsonRegatta.get("Name"));
                regattaResult.setCompetitorGenderType(parseCompetitorGenderType(jsonRegatta, "Gender"));
                regattaResult.setClassName((String) jsonRegatta.get("ClassName"));
                regattaResult.setPdfUrl(parseURL(jsonRegatta, "PdfUrl"));
                regattaResult.setXrrPreliminaryUrl(parseURL(jsonRegatta, "XrrPreliminaryUrl"));
                regattaResult.setXrrFinalUrl(parseURL(jsonRegatta, "XrrFinalUrl"));
                regattaResult.setHtmlUrl(parseURL(jsonRegatta, "HtmlUrl"));
                regattaResult.setPublishedAt(parseDate(jsonRegatta, "Published"));
                regattaResult.setIsFinal((Boolean) jsonRegatta.get("Final"));
                
                JSONArray jsonRaces = (JSONArray) jsonRegatta.get("Races");
                if(jsonRaces != null) {
                    for (Object raceObject: jsonRaces) {
                        RaceResultDescriptor raceResult = new RaceResultDescriptor(); 
                        JSONObject jsonRace = (JSONObject) raceObject;
                        raceResult.setId((String) jsonRace.get("Id"));
                        raceResult.setName((String) jsonRace.get("Name"));
                        raceResult.setStartTimeUTC(parseDate(jsonRace, "StartTimeUTC"));
                        raceResult.setEndTimeUTC(parseDate(jsonRace, "EndTimeUTC"));
                        raceResult.setState((String) jsonRace.get("State"));
                        raceResult.setTracked((Boolean) jsonRace.get("IsTracked"));
                        regattaResult.getRaceResults().add(raceResult);
                    }
                }
                result.getRegattaResults().add(regattaResult);
            }
            is.close();
        } catch(ParseException e) {
            e.printStackTrace();
        } finally { 
            is.close();
        }
        return result;
    }

    private CompetitorGenderType parseCompetitorGenderType(JSONObject jsonDate, String attributeName) {
        CompetitorGenderType result = null;
        String genderTypeAsString = (String) jsonDate.get(attributeName);
        if(genderTypeAsString != null) {
            switch(genderTypeAsString) {
                case "M" : result = CompetitorGenderType.Men; break;
                case "W" : result = CompetitorGenderType.Women; break;
                case "X" : result = CompetitorGenderType.Mixed; break;
                case "Open" : result = CompetitorGenderType.Open; break;
            }
        }
        return result;
    }

    private Integer parseInteger(JSONObject jsonNumber, String attributeName) {
        Integer result = null;
        Number asNumber = (Number) jsonNumber.get(attributeName);
        if(asNumber != null) {
            result = asNumber.intValue();
        }
        return result;
    }

    private Date parseDate(JSONObject jsonDate, String attributeName) {
        Date result = null;
        String dateAsString = (String) jsonDate.get(attributeName);
        if(dateAsString != null && !dateAsString.isEmpty()) {
            try {
                result = DateParser.parseUTC(dateAsString);
            } catch (InvalidDateException e) {
            } 
        }
        return result;
    }

    private URL parseURL(JSONObject jsonURL, String attributeName) {
        URL result = null;
        String urlAsString = (String) jsonURL.get(attributeName);
        if(urlAsString != null && !urlAsString.isEmpty()) {
            try {
                result = new URL(urlAsString);
            } catch (MalformedURLException e) {
            }
        }
        return result;
    }
}
