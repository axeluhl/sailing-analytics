package com.sap.sailing.domain.igtimiadapter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.sap.sailing.domain.igtimiadapter.datatypes.AntHrm;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.datatypes.GpsLatLong;
import com.sap.sailing.domain.igtimiadapter.datatypes.GpsQualitySatCount;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class FixFactoryTest {
    @Rule public Timeout AbstractTracTracLiveTestTimeout = Timeout.millis(2 * 60 * 1000);

    private static final String demoJson = "{\n"+
            "\"EA-AK-AAAG\": {\n"+
                "\"1\": {\n"+
                    "\"1\": [\n"+
                        "-160.123,\n"+
                        "-160.1233\n"+
                    "],\n"+
                    "\"2\": [\n"+
                        "40.43,\n"+
                        "40.44\n"+
                    "],\n"+
                    "\"t\": [\n"+
                        "12345677000,\n"+
                        "12345677500\n"+
                    "]\n"+
                "},\n"+
                "\"3\": {\n"+
                    "\"1\": [\n"+
                        "21\n"+
                    "],\n"+
                    "\"t\": [\n"+
                        "12345677500\n"+
                    "]\n"+
                "}\n"+
            "},\n"+
            "\"EA-AK-AAAH\": {\n"+
                "\"1\": {\n"+
                    "\"1\": [\n"+
                        "-160.12\n"+
                    "],\n"+
                    "\"2\": [\n"+
                        "40.434\n"+
                    "],\n"+
                    "\"t\": [\n"+
                        "12345677000\n"+
                    "]\n"+
                "},\n"+
                "\"13:100\": {\n"+
                    "\"1\": [\n"+
                        "23766\n"+
                        "24619\n"+
                    "],\n"+
                    "\"2\": [\n"+
                        "105\n"+
                        "106\n"+
                    "],\n"+
                    "\"3\": [\n"+
                        "72\n"+
                        "72\n"+
                    "],\n"+
                    "\"t\": [\n"+
                        "1385628380500\n"+
                        "1385628381000\n"+
                    "]\n"+
                "},\n"+
                "\"13:201\": {\n"+
                    "\"1\": [\n"+
                        "24619\n"+
                    "],\n"+
                    "\"2\": [\n"+
                        "106\n"+
                    "],\n"+
                    "\"3\": [\n"+
                        "119\n"+
                    "],\n"+
                    "\"t\": [\n"+
                        "1385628381000\n"+
                    "]\n"+
                "}\n"+
            "}\n"+
        "}";
    
    private JSONObject json;
    
    @Before
    public void setUp() throws ParseException {
        json = (JSONObject) new JSONParser().parse(demoJson);
    }
    
    @Test
    public void testFixProduction() {
        FixFactory fixFactory = new FixFactory();
        Iterable<Fix> fixes = fixFactory.createFixes(json);
        assertFalse(Util.isEmpty(fixes));
        final Iterator<Fix> fixIter = fixes.iterator();
        GpsLatLong firstFix = (GpsLatLong) fixIter.next();
        assertEquals(new MillisecondsTimePoint(12345677000l), firstFix.getTimePoint());
        assertEquals(-160.123, firstFix.getPosition().getLngDeg(), 0.0000001);
        assertEquals(40.43, firstFix.getPosition().getLatDeg(), 0.00000001);
        GpsLatLong secondFix = (GpsLatLong) fixIter.next();
        assertEquals(new MillisecondsTimePoint(12345677500l), secondFix.getTimePoint());
        assertEquals(-160.1233, secondFix.getPosition().getLngDeg(), 0.0000001);
        assertEquals(40.44, secondFix.getPosition().getLatDeg(), 0.00000001);
        GpsQualitySatCount satCount = (GpsQualitySatCount) fixIter.next();
        assertEquals(21, satCount.getSatCount());
        fixIter.next();
        AntHrm hrm = (AntHrm) fixIter.next();
        assertEquals(100, hrm.getSensor().getSensorId());
        assertEquals(72, hrm.getHeartRate());
    }
}
