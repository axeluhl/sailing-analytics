package eventimport;

import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class EventParser {
    
    public EventResults parseEvent(String url){
        
        EventResults eventResults = null;
        
        JSONObject jsonRoot;
        try {
            InputStreamReader is = new InputStreamReader(getStreamReader(url), "UTF-8");
            
            jsonRoot = (JSONObject) new JSONParser().parse(is);
            eventResults = new EventResults();
            eventResults.setId((String) jsonRoot.get("Id"));
            eventResults.setName((String) jsonRoot.get("Name"));
            eventResults.setXrrUrl((String) jsonRoot.get("XrrUrl"));
            
            JSONArray jsonRegattas = (JSONArray) jsonRoot.get("Regattas");
            for (Object regattaObject: jsonRegattas) {
                Regattas regattas = new Regattas();
                JSONObject jsonRegatta = (JSONObject) regattaObject;
                regattas.setId((String) jsonRegatta.get("Id"));
                regattas.setName((String) jsonRegatta.get("Name"));
                regattas.setBoatClass((String) jsonRegatta.get("ClassName"));
                regattas.setGender((String) jsonRegatta.get("Gender"));
                regattas.setXrrEntriesUrl((String) jsonRegatta.get("XrrEntriesUrl"));
                regattas.setXrrPreliminaryUrl((String) jsonRegatta.get("XrrPreliminaryUrl"));
                regattas.setXrrFinalUrl((String) jsonRegatta.get("XrrFinalUrl"));
                regattas.setHtmlUrl((String) jsonRegatta.get("HtmlUrl"));
                
                eventResults.addRegatta(regattas);
            
            }
            
            is.close();
            
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return eventResults;
    }
    
    private InputStream getStreamReader(String url){
        
        InputStream is = null;
        
        URLConnection connection = null;
        try {
            connection = new URL(url).openConnection();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            is = connection.getInputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return is;
        
    }
}
