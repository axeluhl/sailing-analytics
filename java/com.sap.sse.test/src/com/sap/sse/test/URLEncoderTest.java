package com.sap.sse.test;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.junit.Test;

public class URLEncoderTest {
    @Test
    public void testWithSlash() throws UnsupportedEncodingException {
        assertEquals("a%2Fb", URLEncoder.encode("a/b", "UTF-8"));
    }

    @Test
    public void testWithColon() throws UnsupportedEncodingException {
        assertEquals("a%3Ab", URLEncoder.encode("a:b", "UTF-8"));
    }
}
