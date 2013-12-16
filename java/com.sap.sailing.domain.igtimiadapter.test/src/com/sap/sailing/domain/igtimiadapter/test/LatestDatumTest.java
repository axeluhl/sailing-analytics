package com.sap.sailing.domain.igtimiadapter.test;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.datatypes.Type;
import com.sap.sailing.domain.igtimiadapter.impl.FixFactory;

public class LatestDatumTest {

    private final String sample1 = "{\"latest_data\":[{\"latest_datum\":{\"serial_number\":\"DD-EE-AAGA\",\"resource_id\":1012353,\"c1\":0.5556,\"timestamp\":1384091813000.0}},{\"latest_datum\":{\"serial_number\":\"DD-EE-AAHG\",\"resource_id\":1012356,\"c1\":0.3704,\"timestamp\":1384078160000.0}}]}";
    private final String sample2 = "{\"latest_data\":[{\"latest_datum\":{\"serial_number\":\"DD-EE-AAHG\",\"resource_id\":1012356,\"c1\":4.0744,\"timestamp\":1384078157500.0}}]}";
    
    @Test
    public void testFixProductionFromSample1() throws ParseException {
        Iterable<Fix> fixes = new FixFactory().createFixesFromLastDatum((JSONObject) new JSONParser().parse(sample1), Type.SOG);
        assertEquals(2, Util.size(fixes));
        Iterator<Fix> i = fixes.iterator();
        Fix fix1 = i.next();
        assertEquals("DD-EE-AAGA", fix1.getSensor().getDeviceSerialNumber());
        assertEquals(Type.SOG, fix1.getType());
        Fix fix2 = i.next();
        assertEquals("DD-EE-AAHG", fix2.getSensor().getDeviceSerialNumber());
        assertEquals(Type.SOG, fix2.getType());
    }
    
    @Test
    public void testFixProductionFromSample2() throws ParseException {
        Iterable<Fix> fixes = new FixFactory().createFixesFromLastDatum((JSONObject) new JSONParser().parse(sample2), Type.AWS);
        assertEquals(1, Util.size(fixes));
        Iterator<Fix> i = fixes.iterator();
        Fix fix1 = i.next();
        assertEquals("DD-EE-AAHG", fix1.getSensor().getDeviceSerialNumber());
        assertEquals(Type.AWS, fix1.getType());
    }
}
