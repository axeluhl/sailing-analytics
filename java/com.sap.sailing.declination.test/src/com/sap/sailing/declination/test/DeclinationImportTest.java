package com.sap.sailing.declination.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.util.regex.Matcher;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.sap.sailing.declination.Declination;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class DeclinationImportTest extends AbstractDeclinationTest {
    @Test
    public void testRegexpDeclination() {
        Matcher declinationMatcher = importer.getDeclinationPattern().matcher("    <p class=\"indent\"><b>Declination</b> = 12&deg; 41' W");
        assertTrue(declinationMatcher.find());
        assertEquals("12", declinationMatcher.group(1));
        assertEquals("41", declinationMatcher.group(2));
        assertEquals("W", declinationMatcher.group(3));
    }
    
    @Test
    public void testRegexpAnnualChange() {
        Matcher annualChangeMatcher = importer.getAnnualChangePattern().matcher("    changing by 0&deg; 11' E/year </p>");
        assertTrue(annualChangeMatcher.find());
        assertEquals("0", annualChangeMatcher.group(1));
        assertEquals("11", annualChangeMatcher.group(2));
        assertEquals("E", annualChangeMatcher.group(3));
    }
    
    @Test
    public void importSimpleDeclination() throws IOException, ParseException, ParserConfigurationException, SAXException {
        Declination record = importer.importRecord(new DegreePosition(53, 3),
                new MillisecondsTimePoint(simpleDateFormat.parse("2016-05-27").getTime()));
        assertEquals(0.26307, record.getBearing().getDegrees(), 0.0001);
        assertEquals(0.14795, record.getAnnualChange().getDegrees(), 0.0001);
    }

    @Test
    public void importSouthernHemisphereDeclination() throws IOException, ParseException, ParserConfigurationException, SAXException {
        long start = System.currentTimeMillis();
        Declination record = importer.importRecord(new DegreePosition(-10, 3),
                new MillisecondsTimePoint(simpleDateFormat.parse("2017-05-27").getTime()));
        System.out.println("took "+(System.currentTimeMillis()-start)+"ms");
        assertEquals(-8.44581, record.getBearing().getDegrees(), 0.0001);
        assertEquals(0.17712, record.getAnnualChange().getDegrees(), 0.0001);
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
