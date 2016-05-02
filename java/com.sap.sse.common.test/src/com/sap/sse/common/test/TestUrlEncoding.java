package com.sap.sse.common.test;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.junit.Test;

public class TestUrlEncoding {
    @Test
    public void testUrlQueryStringEncoding() throws UnsupportedEncodingException {
        final String s = "Test & Test";
        assertEquals("Test+%26+Test", URLEncoder.encode(s, "UTF-8"));
        assertEquals(s, URLDecoder.decode(URLEncoder.encode(s, "UTF-8"), "UTF-8"));
    }
}
