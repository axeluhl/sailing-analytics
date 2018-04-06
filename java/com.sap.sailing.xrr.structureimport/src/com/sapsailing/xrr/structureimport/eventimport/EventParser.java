package com.sapsailing.xrr.structureimport.eventimport;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sse.util.HttpUrlConnectionHelper;

public class EventParser {
    private static final Logger logger = Logger.getLogger(EventParser.class.getName());
    
    public EventResults parseEvent(String url){
        EventResults eventResults = null;
        JSONObject jsonRoot;
        try {
            InputStreamReader streamReader = getStreamReader(url);
            
            jsonRoot = (JSONObject) new JSONParser().parse(streamReader);
            String id = (String) jsonRoot.get("Id");
            String name = (String) jsonRoot.get("Name");
            String xrrUrl = (String) jsonRoot.get("XrrUrl");
            eventResults = new EventResults(id, name, xrrUrl);
            JSONArray jsonRegattas = (JSONArray) jsonRoot.get("Regattas");
            for (Object regattaObject: jsonRegattas) {
                RegattaJSON regatta = new RegattaJSON();
                JSONObject jsonRegatta = (JSONObject) regattaObject;
                regatta.setId((String) jsonRegatta.get("Id"));
                regatta.setName((String) jsonRegatta.get("Name"));
                regatta.setBoatClass((String) jsonRegatta.get("ClassName"));
                regatta.setGender((String) jsonRegatta.get("Gender"));
                regatta.setXrrEntriesUrl((String) jsonRegatta.get("XrrEntriesUrl"));
                regatta.setXrrPreliminaryUrl((String) jsonRegatta.get("XrrPreliminaryUrl"));
                regatta.setXrrFinalUrl((String) jsonRegatta.get("XrrFinalUrl"));
                regatta.setHtmlUrl((String) jsonRegatta.get("HtmlUrl"));
                eventResults.addRegatta(regatta);
            }
            streamReader.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during parsing event structure", e);
        }
        return eventResults;
    }
    
    private InputStreamReader getStreamReader(String url) throws UnsupportedEncodingException{
        InputStream is = null;
        URLConnection connection = null;
        try {
            connection = HttpUrlConnectionHelper.redirectConnection(new URL(url));
            is = connection.getInputStream();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during connecting to event structure url "+url, e);
        }
        return new InputStreamReader(is, "UTF-8");
    }
}
