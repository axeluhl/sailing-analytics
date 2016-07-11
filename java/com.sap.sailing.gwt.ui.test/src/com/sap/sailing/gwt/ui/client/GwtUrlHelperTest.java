package com.sap.sailing.gwt.ui.client;

import org.junit.Test;

import com.google.gwt.junit.client.GWTTestCase;
import com.sap.sse.common.util.UrlHelper;

public class GwtUrlHelperTest extends GWTTestCase {
    
    private final UrlHelper urlHelper = GwtUrlHelper.INSTANCE;
    
    @Test
    public void testEncodeQueryStringSimple() {
        testCase("TestTest", "TestTest");
    }
    
    @Test
    public void testEncodeQueryStringWithWhitespace() {
        testCase("Test%20Test", "Test Test");
    }
    
    @Test
    public void testEncodeQueryStringWithSlash() {
        testCase("Test%2FTest", "Test/Test");
    }
    
    @Test
    public void testEncodeQueryStringWithPlusSign() {
        testCase("Test%2BTest", "Test+Test");
    }
    
    @Test
    public void testEncodeQueryStringWithAmpersand() {
        testCase("Test%26Test", "Test&Test");
    }
    
    @Test
    public void testEncodeQueryStringComplex() {
        testCase("Test%20%2B%20Test%20%2F%20Test%20%26%20Test", "Test + Test / Test & Test");
    }
    
    private void testCase(String expectedEncodedString, String decodedString) {
        assertEquals(expectedEncodedString, urlHelper.encodeQueryString(decodedString));
    }

    @Override
    public String getModuleName() {
        return "com.sap.sailing.gwt.ui.test.TestConsole";
    }

}
