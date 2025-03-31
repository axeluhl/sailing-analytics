package com.sap.sailing.nmeaconnector.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.nmeaconnector.NmeaFactory;
import com.sap.sse.common.Util;

public class NMEAWindImportTest {
    Iterable<Wind> windFixes;
    
    @Before
    public void setUp() throws FileNotFoundException, InterruptedException {
        NmeaFactory.INSTANCE.getUtil().registerAdditionalParsers();
        windFixes = NmeaFactory.INSTANCE.readWind(new FileInputStream("resources/ExcerptFromLogJB010815.txt"));
    }
    
    @Test
    public void testThatSomeWindWasFound() {
        assertNotNull(windFixes);
        assertTrue(!Util.isEmpty(windFixes));
    }
    
    @After
    public void tearDown() {
        NmeaFactory.INSTANCE.getUtil().unregisterAdditionalParsers();
    }
}
