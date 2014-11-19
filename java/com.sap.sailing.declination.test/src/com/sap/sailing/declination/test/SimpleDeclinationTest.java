package com.sap.sailing.declination.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.sap.sailing.declination.Declination;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class SimpleDeclinationTest extends AbstractDeclinationTest {
    @Test
    public void testSimpleCorrection() throws IOException, ParseException, ParserConfigurationException, SAXException {
        Declination record = importer.importRecord(new DegreePosition(53, 3),
                new MillisecondsTimePoint(simpleDateFormat.parse("1920-05-27").getTime()));
        assertEquals(-12.68941, record.getBearing().getDegrees(), 0.0001);
        assertEquals(0.1863178, record.getAnnualChange().getDegrees(), 0.0001);
        TimePoint oneYearLater = new MillisecondsTimePoint(simpleDateFormat.parse("1921-05-27").getTime());
        Bearing bearing = record.getBearingCorrectedTo(oneYearLater);
        assertEquals(-12.68941+0.1863178, bearing.getDegrees(), 0.0001);
    }

    @Test
    public void testBackwardCorrection() throws IOException, ParseException, ParserConfigurationException, SAXException {
        Declination record = importer.importRecord(new DegreePosition(53, 3),
                new MillisecondsTimePoint(simpleDateFormat.parse("1920-05-27").getTime()));
        assertEquals(-12.68941, record.getBearing().getDegrees(), 0.0001);
        assertEquals(0.1863178, record.getAnnualChange().getDegrees(), 0.0001);
        // 1920 is a leap year, so add one day, otherwise it wouldn't match the 365 day assumption
        TimePoint oneYearEarlier = new MillisecondsTimePoint(simpleDateFormat.parse("1919-05-28").getTime());
        Bearing bearing = record.getBearingCorrectedTo(oneYearEarlier);
        assertEquals(-12.68941-0.1863178, bearing.getDegrees(), 0.0001);
    }

}
