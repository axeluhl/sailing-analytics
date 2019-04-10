package com.sap.sailing.domain.ranking;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ORCCertificateImporterJSON implements ORCCertificateImporter{
    
    private JSONObject main;

    ORCCertificateImporterJSON () {
    }

    public void readJsonFromSrc(String src) throws IOException, ParseException, FileNotFoundException {
        Object obj = new JSONParser().parse(new FileReader(src));
        main = (JSONObject) obj;
    }
    
    public void readJsonFromUrl(String url) throws IOException, ParseException {
        InputStream is = new URL(url).openStream();
        Object obj = new JSONParser().parse(new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)));
        main = (JSONObject) obj;
    }

    @Override
    public void importData() {
        
    }

    @Override
    public ORCCertificate getCertificate(String sailnumber) {
        return null;
    }

    @Override
    public Map<String, ORCCertificate> getCertificates(String[] sailnumbers) {
        return null;
    }
}