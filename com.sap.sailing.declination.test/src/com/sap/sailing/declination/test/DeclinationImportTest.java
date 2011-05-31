package com.sap.sailing.declination.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.declination.DeclinationRecord;
import com.sap.sailing.declination.NOAAImporter;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;

public class DeclinationImportTest {
    private NOAAImporter importer;
    
    @Before
    public void setUp() {
        importer = new NOAAImporter();
    }
    
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
    public void importSimpleDeclination() throws IOException, ParseException {
        DeclinationRecord record = importer.importRecord(new DegreePosition(53, 3),
                new MillisecondsTimePoint(new SimpleDateFormat("yyyy-MM-dd").parse("1920-05-27").getTime()));
        assertEquals(-12.-41./60., record.getBearing().getDegrees(), 0.000000001);
        assertEquals(0.+11./60., record.getAnnualChange().getDegrees(), 0.000000001);
    }

    @Test
    public void importSouthernHemisphereDeclination() throws IOException, ParseException {
        long start = System.currentTimeMillis();
        DeclinationRecord record = importer.importRecord(new DegreePosition(-10, 3),
                new MillisecondsTimePoint(new SimpleDateFormat("yyyy-MM-dd").parse("2011-05-27").getTime()));
        System.out.println("took "+(System.currentTimeMillis()-start)+"ms");
        assertEquals(-9.-32./60., record.getBearing().getDegrees(), 0.000000001);
        assertEquals(0.+10./60., record.getAnnualChange().getDegrees(), 0.000000001);
    }
    
    @Test
    public void readOnlineOrFromFile() throws IOException, ClassNotFoundException, ParseException {
        DeclinationRecord declination = importer.getDeclination(new DegreePosition(53, 3),
                new MillisecondsTimePoint(new SimpleDateFormat("yyyy-MM-dd").parse("2011-05-27").getTime()), 
                /* timeoutForOnlineFetchInMilliseconds */ 1000);
        assertNotNull(declination);
        System.out.println(declination);
    }
}
