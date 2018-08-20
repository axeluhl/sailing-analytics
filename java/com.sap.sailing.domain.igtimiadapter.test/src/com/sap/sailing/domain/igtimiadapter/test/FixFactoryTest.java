package com.sap.sailing.domain.igtimiadapter.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.impl.FixFactory;
import com.sap.sse.common.Util;

public class FixFactoryTest {
    private FixFactory fixFactory;
    
    @Before
    public void setUp() {
        fixFactory = new FixFactory();
    }
    
    @Test
    public void testUnknownFixTypes() throws ParseException {
        final String[] messages = new String[] {
                               "{\"DC-FE-AAKG\":{\"1\":{\"t\":[1510174025500],\"1\":[170.5044476],\"2\":[-45.87697]}}}",
                               "{\"DC-FE-AAKG\":{\"2\":{\"t\":[1510174025500],\"1\":[1]}}}",
                               "{\"DC-FE-AAKG\":{\"3\":{\"t\":[1510174025500],\"1\":[10]}}}",
                               "{\"DC-FE-AAKG\":{\"7\":{\"t\":[1510174025500],\"1\":[187.7]}}}",
                               "{\"DC-FE-AAKG\":{\"11\":{\"t\":[1510174025500],\"1\":[54]}}}",
                               "{\"DC-FE-AAKG\":{\"12\":{\"t\":[1510174025500],\"1\":[1.852]}}}",
                               "{\"DC-FE-AAKG\":{\"22\":{\"t\":[1510174025500],\"1\":[263.4]}}}",
                               "{\"DC-FE-AAKG\":{\"23\":{\"t\":[1510174025500],\"1\":[1.852]}}}",
                               "{\"DC-FE-AAKG\":{\"6\":{\"t\":[1510174025500],\"1\":[0]}}}",
                               "{\"DC-FE-AAKG\":{\"9\":{\"t\":[1510174025500],\"1\":[0]}}}" };
        List<Fix> fixes = new ArrayList<>();
        for (final String message : messages) {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(message);
            Util.addAll(fixFactory.createFixes(jsonObject), fixes);
        }
        assertTrue(!Util.isEmpty(fixes)); // this largely asserts that no exception was thrown although there were unknown fix types (22/23)
    }
}
