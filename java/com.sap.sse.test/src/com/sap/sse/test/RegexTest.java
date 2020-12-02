package com.sap.sse.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class RegexTest {
    private static final Logger logger = Logger.getLogger(RegexTest.class.getName());
    private final Pattern trailingVersionPattern = Pattern.compile("^(.*) ([0-9]+)\\.([0-9]+)(\\.([0-9]+))?$");
    
    @Test
    public void test1_0() {
        final Matcher matcher = trailingVersionPattern.matcher("Image 1.0");
        assertTrue(matcher.matches());
        assertEquals(5, matcher.groupCount());
        assertEquals("Image", matcher.group(1));
        assertEquals("1", matcher.group(2));
        assertEquals("0", matcher.group(3));
        assertNull(matcher.group(4));
        assertNull(matcher.group(5));
    }

    @Test
    public void test4_5_17() {
        final Matcher matcher = trailingVersionPattern.matcher("Image Bla 4.5.17");
        assertTrue(matcher.matches());
        assertEquals(5, matcher.groupCount());
        assertEquals("Image Bla", matcher.group(1));
        assertEquals("4", matcher.group(2));
        assertEquals("5", matcher.group(3));
        assertEquals(".17", matcher.group(4));
        assertEquals("17", matcher.group(5));
    }

    @Test
    public void testEscapingForEcho() {
        final String value = "abc\\'\"\\\"";
        logger.info("Unescaped value: "+value);
        final String escaped = "\""+value.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("'", "\\\\'")+"\"";
        logger.info("Escaped value: "+escaped);
        assertEquals("\"abc\\\\\\'\\\"\\\\\\\"\"", escaped);
    }
}
