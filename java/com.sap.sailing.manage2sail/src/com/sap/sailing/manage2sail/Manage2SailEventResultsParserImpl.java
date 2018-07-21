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
import com.sap.sse.InvalidDateException;
import com.sap.sse.util.DateParser;

/**
 * URLs should be of the form
 * <pre>
 * http://manage2sail.com/api/public/links/event/d30883d3-2876-4d7e-af49-891af6cbae1b?accesstoken=bDAv8CwsTM94ujZ&mediaType=json
 * </pre>
 * where the UUID following the <code>event</code> path element represents the event ID. Events can be
 * discovered by the manage2sail.com website.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class Manage2SailEventResultsParserImpl implements Manage2SailEventResultsParser {

    /**
     * @param is closed before the method returns, also in case of exception
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
            JSONObject jsonTracking = (JSONObject) jsonRoot.get("Tracking");
            if (jsonTracking != null) {
                result.setTrackingDataHost((String) jsonTracking.get("Host"));
                result.setTrackingDataPort(parseInteger(jsonTracking, "Port"));
            }
            
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
                regattaResult.setXrrEntriesUrl(parseURL(jsonRegatta, "XrrEntriesUrl"));
                regattaResult.setHtmlUrl(parseURL(jsonRegatta, "HtmlUrl"));
                regattaResult.setPublishedAt(parseDate(jsonRegatta, "Published"));
                regattaResult.setIsFinal((Boolean) jsonRegatta.get("Final"));
                JSONArray jsonRaces = (JSONArray) jsonRegatta.get("Races");
                if (jsonRaces != null) {
                    for (Object raceObject: jsonRaces) {
                        RaceResultDescriptor raceResult = new RaceResultDescriptor(); 
                        JSONObject jsonRace = (JSONObject) raceObject;
                        raceResult.setId((String) jsonRace.get("Id"));
                        raceResult.setName((String) jsonRace.get("Name"));
                        raceResult.setRaceColumnNumber(parseInteger(jsonRace, "RaceIndex"));
                        raceResult.setStatus((String) jsonRace.get("Status"));
                        raceResult.setSeriesName((String) jsonRace.get("Series"));
                        raceResult.setFleetName((String) jsonRace.get("Fleet"));
                        raceResult.setTracked((Boolean) jsonRace.get("IsTracked"));
                        raceResult.setStartTime(parseDate(jsonRace, "StartTime"));
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
        Object object = jsonNumber.get(attributeName);
        if(object != null) {
        	if(object instanceof Number) {
                result = ((Number) object).intValue();
        	} else if (object instanceof String) {
        		String numberAsString = (String) object;
        		if(!numberAsString.isEmpty()) {
            		try {
            			result = Integer.parseInt(numberAsString);   
            		} catch (NumberFormatException e) {
            		}
        		}
        	}
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
