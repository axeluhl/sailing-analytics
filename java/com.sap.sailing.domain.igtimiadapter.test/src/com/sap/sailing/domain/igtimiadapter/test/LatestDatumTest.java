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

    private final String sample1 = "{\"DD-EE-AAGA\":{\"9\":{\"t\":[1.384091813E12],\"resource_id\":1012353,\"1\":[0.5556]}},\"DD-EE-AAHG\":{\"9\":{\"t\":[1.386186319E12],\"resource_id\":1012677,\"1\":[0.0]}}}";
    
    @Test
    public void testFixProductionFromSample1() throws ParseException {
        Iterable<Fix> fixes = new FixFactory().createFixes((JSONObject) new JSONParser().parse(sample1));
        assertEquals(2, Util.size(fixes));
        Iterator<Fix> i = fixes.iterator();
        Fix fix1 = i.next();
        assertEquals("DD-EE-AAGA", fix1.getSensor().getDeviceSerialNumber());
        assertEquals(Type.SOG, fix1.getType());
        Fix fix2 = i.next();
        assertEquals("DD-EE-AAHG", fix2.getSensor().getDeviceSerialNumber());
        assertEquals(Type.SOG, fix2.getType());
    }
}
