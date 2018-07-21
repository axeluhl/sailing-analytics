package com.sap.sailing.declination.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.sap.sailing.declination.Declination;
import com.sap.sailing.declination.impl.DeclinationImporter;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public abstract class SimpleDeclinationTest<I extends DeclinationImporter> extends AbstractDeclinationTest<I> {
    @Test
    public void testSimpleCorrection() throws IOException, ParseException, ParserConfigurationException, SAXException {
        final MillisecondsTimePoint timePoint = new MillisecondsTimePoint(simpleDateFormat.parse("2019-05-27").getTime());
        Declination record = importer.importRecord(new DegreePosition(53, 3), timePoint);
        assertEquals(0.70594, record.getBearing().getDegrees(), 0.001);
        assertEquals(0.14745, record.getAnnualChange().getDegrees(), 0.001);
        TimePoint oneYearLater = timePoint.plus(Duration.ONE_YEAR);
        Bearing bearing = record.getBearingCorrectedTo(oneYearLater);
        assertEquals(0.70594+0.14745, bearing.getDegrees(), 0.01);
    }

    @Test
    public void testBackwardCorrection() throws IOException, ParseException, ParserConfigurationException, SAXException {
        final MillisecondsTimePoint timePoint = new MillisecondsTimePoint(simpleDateFormat.parse("2019-05-27").getTime());
        Declination record = importer.importRecord(new DegreePosition(53, 3), timePoint);
        assertEquals(0.70594, record.getBearing().getDegrees(), 0.001);
        assertEquals(0.14745, record.getAnnualChange().getDegrees(), 0.001);
        TimePoint oneYearEarlier = timePoint.minus(Duration.ONE_YEAR);
        Bearing bearing = record.getBearingCorrectedTo(oneYearEarlier);
        assertEquals(0.70594-0.14745, bearing.getDegrees(), 0.01);
    }
}
