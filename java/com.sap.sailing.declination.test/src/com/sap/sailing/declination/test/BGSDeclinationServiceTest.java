package com.sap.sailing.declination.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.declination.Declination;
import com.sap.sailing.declination.impl.BGSImporter;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class BGSDeclinationServiceTest extends DeclinationServiceTest<BGSImporter> {
    @Override
    @Before
    public void setUp() {
        importer = new BGSImporter();
        super.setUp();
    }

    @Test
    public void testDeclinationQueryNotMatchedInStore() throws IOException, ClassNotFoundException, ParseException {
        Declination result = service.getDeclination(new MillisecondsTimePoint(simpleDateFormat.parse("2020-02-03").getTime()),
                new DegreePosition(51, -5), /* timeoutForOnlineFetchInMilliseconds */ 5000);
        assertNotNull(result);
        assertEquals(-1.63178, result.getBearing().getDegrees(), 0.12);
        assertEquals(0.19389, result.getAnnualChange().getDegrees(), 0.01);
    }
}
