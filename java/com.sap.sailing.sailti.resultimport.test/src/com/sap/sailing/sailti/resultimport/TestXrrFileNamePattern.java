package com.sap.sailing.sailti.resultimport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;

import org.junit.Test;

import com.sap.sse.common.TimePoint;

public class TestXrrFileNamePattern {
    @Test
    public void testXrrFileNamePatternWithSimpleExample() throws ParseException {
        final SailtiEventResultsParserImpl parser = new SailtiEventResultsParserImpl();
        final String sample = "XML-Pelicano_131_5430_20220314190806.xml";
        final Matcher matcher = SailtiEventResultsParserImpl.xrrFileNamePattern.matcher(sample);
        assertTrue(matcher.matches());
        assertEquals("Pelicano", parser.getBoatClassName(matcher));
        final TimePoint expectedPublishedAtTimePoint = TimePoint.of(new SimpleDateFormat("yyyyMMddhhmmssX").parse("20220314190806Z"));
        assertEquals(expectedPublishedAtTimePoint, parser.getTimePoint(matcher));
    }
}
