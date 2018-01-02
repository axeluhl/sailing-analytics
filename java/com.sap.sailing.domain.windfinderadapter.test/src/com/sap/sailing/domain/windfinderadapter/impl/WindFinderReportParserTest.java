package com.sap.sailing.domain.windfinderadapter.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.windfinderadapter.Spot;
import com.sap.sse.common.Util;

public class WindFinderReportParserTest {
    private final String TEST_MESSAGE = "[{\"wg\":16,\"wd\":331,\"dtl\":\"2017-11-13T15:16:00+01:00\",\"dtl_s\":\"2017-11-13T15:15:30+01:00\",\"ws\":12,\"at\":7.0},{\"wg\":14,\"wd\":333,\"dtl\":\"2017-11-13T15:32:00+01:00\",\"dtl_s\":\"2017-11-13T15:31:30+01:00\",\"ws\":12,\"at\":7.0},{\"wg\":16,\"wd\":332,\"dtl\":\"2017-11-13T15:40:00+01:00\",\"dtl_s\":\"2017-11-13T15:39:30+01:00\",\"ws\":12,\"at\":7.0}]";

    @Test
    public void testReadingOneFix() throws ParseException, NumberFormatException, java.text.ParseException {
        final JSONArray fullJson = (JSONArray) new JSONParser().parse(TEST_MESSAGE);
        assertEquals(3, fullJson.size());
        final Wind wind = new WindFinderReportParser().parse(new DegreePosition(54.47, 10.28), (JSONObject) fullJson.get(0));
        assertEquals(12.0, wind.getKnots(), 0.0000001);
        assertEquals(331.0, wind.getFrom().getDegrees(), 0.0000001);
        assertPositionEquals(new DegreePosition(54.47, 10.28), wind.getPosition(), 0.000001);
    }
    
    @Test
    public void testReadingSeveralFixesFromStream() throws ParseException, NumberFormatException, java.text.ParseException, IOException {
        final Reader reader = new InputStreamReader(getClass().getResourceAsStream("/sap_schilksee_10044N.json"));
        final JSONArray fullJson = (JSONArray) new JSONParser().parse(reader);
        assertEquals(3, fullJson.size());
        final Wind wind = new WindFinderReportParser().parse(new DegreePosition(54.47, 10.28), (JSONObject) fullJson.get(0));
        assertEquals(12.0, wind.getKnots(), 0.0000001);
        assertEquals(331.0, wind.getFrom().getDegrees(), 0.0000001);
        assertPositionEquals(new DegreePosition(54.47, 10.28), wind.getPosition(), 0.000001);
    }
    
    @Test
    public void testReadingSpotDescriptions() throws IOException, ParseException {
        final Reader reader = new InputStreamReader(getClass().getResourceAsStream("/schilksee_nearby.json"));
        final JSONArray fullJson = (JSONArray) new JSONParser().parse(reader);
        assertEquals(2, fullJson.size());
        final Iterable<Spot> spots = new WindFinderReportParser().parseSpots(fullJson);
        final Spot kielHoltenau = (Spot) Util.get(spots, 0);
        final Spot kielLeuchtturm = (Spot) Util.get(spots, 1);
        assertEquals("Kiel-Holtenau Airport", kielHoltenau.getName());
        assertEquals("kiel-holtenau", kielHoltenau.getKeyword());
        assertEquals("de15", kielHoltenau.getId());
        assertPositionEquals(new DegreePosition(54.38, 10.15), kielHoltenau.getPosition(), 0.000001);
        assertEquals("Kiel/Leuchtturm", kielLeuchtturm.getName());
        assertEquals("kiel_leuchtturm", kielLeuchtturm.getKeyword());
        assertEquals("10044N", kielLeuchtturm.getId());
        assertPositionEquals(new DegreePosition(54.47, 10.28), kielLeuchtturm.getPosition(), 0.000001);
    }

    private static void assertPositionEquals(Position p1, Position p2, double degreeDelta) {
        assertEquals(p1.getLatDeg(), p2.getLatDeg(), degreeDelta);
        assertEquals(p1.getLngDeg(), p2.getLngDeg(), degreeDelta);
    }
}