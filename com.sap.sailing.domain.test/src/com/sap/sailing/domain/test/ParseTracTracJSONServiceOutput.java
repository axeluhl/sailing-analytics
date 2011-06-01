package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

public class ParseTracTracJSONServiceOutput {
    @Test
    public void testWaymouth() throws IOException, ParseException {
        JSONObject result = parseJSONObject("/jsonservice_weymouth.php.txt");
        assertNotNull(result);
        JSONObject event = (JSONObject) result.get("event");
        assertNotNull(event);
        assertEquals("Sailing Team Germany", event.get("name"));
        JSONArray races = (JSONArray) result.get("races");
        assertEquals(24, races.size());
        for (Object race : races) {
            JSONObject jsonRace = (JSONObject) race;
            assertNotNull(jsonRace.get("url"));
            assertEquals("weym", ((String) jsonRace.get("name")).substring(0, "weym".length()));
        }
    }

    private JSONObject parseJSONObject(String filename) throws IOException, ParseException {
        InputStream is = getClass().getResourceAsStream(filename);
        assertNotNull(is);
        JSONParser parser = new JSONParser();
        Object result = parser.parse(new InputStreamReader(is));
        assertTrue(result instanceof JSONObject);
        return (JSONObject) result;
    }
    
    @Test
    public void testHamilton() throws IOException, ParseException {
        JSONObject result = parseJSONObject("/jsonservice-505-hamilton.php.txt");
        assertNotNull(result);
        JSONObject event = (JSONObject) result.get("event");
        assertNotNull(event);
        assertEquals("SAP 2011 505 World Championship", event.get("name"));
        JSONArray races = (JSONArray) result.get("races");
        assertEquals(14, races.size());
        for (Object race : races) {
            JSONObject jsonRace = (JSONObject) race;
            assertNotNull(jsonRace.get("url"));
            assertNotNull(jsonRace.get("name"));
        }
    }
}
