package com.sap.sailing.domain.igtimiadapter.impl;

import static org.junit.Assert.assertFalse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;

public class FixFactoryTest {
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
                        "60\n"+
                    "],\n"+
                    "\"t\": [\n"+
                        "12345678000\n"+
                    "]\n"+
                "},\n"+
                "\"13:201\": {\n"+
                    "\"1\": [\n"+
                        "119,\n"+
                        "120\n"+
                    "],\n"+
                    "\"t\": [\n"+
                        "12345677000,\n"+
                        "12345677500\n"+
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
    }
}
