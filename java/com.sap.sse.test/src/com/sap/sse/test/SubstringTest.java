package com.sap.sse.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SubstringTest {
    @Test
    public void substringAfterEndTest() {
        final String abc = "abc?";
        assertEquals("", abc.substring(abc.indexOf("?")+1));
    }
}
