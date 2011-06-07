package com.sap.sailing.declination.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.text.ParseException;

import org.junit.Test;

import com.sap.sailing.declination.Declination;
import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;

public class SimpleDeclinationTest extends AbstractDeclinationTest {
    @Test
    public void testSimpleCorrection() throws IOException, ParseException {
        Declination record = importer.importRecord(new DegreePosition(53, 3),
                new MillisecondsTimePoint(simpleDateFormat.parse("1920-05-27").getTime()));
        assertEquals(-12.-41./60., record.getBearing().getDegrees(), 0.000000001);
        assertEquals(0.+11./60., record.getAnnualChange().getDegrees(), 0.000000001);
        TimePoint oneYearLater = new MillisecondsTimePoint(simpleDateFormat.parse("1921-05-27").getTime());
        Bearing bearing = record.getBearingCorrectedTo(oneYearLater);
        assertEquals(-12.-41./60.+11./60., bearing.getDegrees(), 0.000000001);
    }

}
