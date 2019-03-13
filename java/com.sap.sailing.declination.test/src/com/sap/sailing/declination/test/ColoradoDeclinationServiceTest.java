package com.sap.sailing.declination.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.declination.Declination;
import com.sap.sailing.declination.impl.ColoradoImporter;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ColoradoDeclinationServiceTest extends DeclinationServiceTest<ColoradoImporter> {
    @Override
    @Before
    public void setUp() {
        importer = new ColoradoImporter();
        super.setUp();
    }

    @Test
    public void testDeclinationQueryNotMatchedInStore() throws IOException, ClassNotFoundException, ParseException {
        Declination result = service.getDeclination(new MillisecondsTimePoint(simpleDateFormat.parse("2020-02-03").getTime()),
                new DegreePosition(51, -5), /* timeoutForOnlineFetchInMilliseconds */ 5000);
        assertNotNull(result);
        assertEquals(-1.80464, result.getBearing().getDegrees(), 0.001);
        assertEquals(0.15244, result.getAnnualChange().getDegrees(), 0.001);
    }
}
