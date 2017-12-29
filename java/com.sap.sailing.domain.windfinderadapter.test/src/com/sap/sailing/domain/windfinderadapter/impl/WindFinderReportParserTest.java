package com.sap.sailing.domain.windfinderadapter.impl;

import static org.junit.Assert.assertEquals;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.DegreePosition;

public class WindFinderReportParserTest {
    private final String TEST_MESSAGE = "[{\"wg\":16,\"wd\":331,\"dtl\":\"2017-11-13T15:16:00+01:00\",\"dtl_s\":\"2017-11-13T15:15:30+01:00\",\"ws\":12,\"at\":7.0},{\"wg\":14,\"wd\":333,\"dtl\":\"2017-11-13T15:32:00+01:00\",\"dtl_s\":\"2017-11-13T15:31:30+01:00\",\"ws\":12,\"at\":7.0},{\"wg\":16,\"wd\":332,\"dtl\":\"2017-11-13T15:40:00+01:00\",\"dtl_s\":\"2017-11-13T15:39:30+01:00\",\"ws\":12,\"at\":7.0}]";

    @Test
    public void testReadingOneFix() throws ParseException, NumberFormatException, java.text.ParseException {
        final JSONArray fullJson = (JSONArray) new JSONParser().parse(TEST_MESSAGE);
        assertEquals(3, fullJson.size());
        final Wind wind = new WindFinderReportParser(new DegreePosition(0, 0)).parse((JSONObject) fullJson.get(0));
        assertEquals(12.0, wind.getKnots(), 0.0000001);
        assertEquals(331.0, wind.getFrom().getDegrees(), 0.0000001);
        assertEquals(0.0, wind.getPosition().getLatDeg(), 0.0000001);
        assertEquals(0.0, wind.getPosition().getLngDeg(), 0.0000001);
    }
}