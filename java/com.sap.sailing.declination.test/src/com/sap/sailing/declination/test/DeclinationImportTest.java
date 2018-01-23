package com.sap.sailing.declination.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.sap.sailing.declination.Declination;
import com.sap.sailing.declination.impl.DeclinationImporter;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public abstract class DeclinationImportTest<I extends DeclinationImporter> extends AbstractDeclinationTest<I> {
    
    @Test
    public void importSimpleDeclination() throws IOException, ParseException, ParserConfigurationException, SAXException {
        Declination record = importer.importRecord(new DegreePosition(53, 3),
                new MillisecondsTimePoint(simpleDateFormat.parse("2016-05-27").getTime()));
        assertEquals(0.26307, record.getBearing().getDegrees(), 0.001);
        assertEquals(0.14795, record.getAnnualChange().getDegrees(), 0.001);
    }

    @Test
    public void importSouthernHemisphereDeclination() throws IOException, ParseException, ParserConfigurationException, SAXException {
        long start = System.currentTimeMillis();
        Declination record = importer.importRecord(new DegreePosition(-10, 3),
                new MillisecondsTimePoint(simpleDateFormat.parse("2017-05-27").getTime()));
        System.out.println("took "+(System.currentTimeMillis()-start)+"ms");
        assertEquals(-8.44581, record.getBearing().getDegrees(), 0.001);
        assertEquals(0.17712, record.getAnnualChange().getDegrees(), 0.001);
    }
    
    @Test
    public void readOnlineOrFromFile() throws IOException, ClassNotFoundException, ParseException {
        Declination declination = importer.getDeclination(new DegreePosition(53, 3),
                new MillisecondsTimePoint(simpleDateFormat.parse("2018-05-27").getTime()), 
                /* timeoutForOnlineFetchInMilliseconds */ 10000);
        assertNotNull(declination);
        System.out.println(declination);
    }
    
}
