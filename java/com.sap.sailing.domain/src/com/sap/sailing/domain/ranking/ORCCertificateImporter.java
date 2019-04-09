package com.sap.sailing.domain.ranking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ORCCertificateImporter {
    
    private final JSONObject main;

    ORCCertificateImporter (String url) {
        main = readJsonFromUrl(url);
        
    }
    
    public static JSONObject readJsonFromUrl(String url) {
        try (InputStream is = new URL(url).openStream();) {
          Object obj = new JSONParser().parse(new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)));
          return (JSONObject) obj;
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
      }
    
    private Map<String, JSONObject> createCertificateMap() {
        return null;
    }
}