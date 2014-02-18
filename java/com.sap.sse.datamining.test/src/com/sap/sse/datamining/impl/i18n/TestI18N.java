package com.sap.sse.datamining.impl.i18n;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.test.util.OpenResourceBundleManager;

public class TestI18N {

    private static final String SIMPLE_TEST_MESSAGE_KEY = "SimpleTestMessage";
    private static final String TEST_MESSAGE_WITH_PARAMETERS = "TestMessageWithParameters";
    
    private OpenResourceBundleManager bundleManager;
    
    @Before
    public void initializeBundleManager() {
        bundleManager = new OpenResourceBundleManager(Locale.ENGLISH);
    }

    @Test
    public void testGettingASimpleMessage() {
        assertThat(bundleManager.get("default", SIMPLE_TEST_MESSAGE_KEY), is("English"));
        assertThat(bundleManager.get("en", SIMPLE_TEST_MESSAGE_KEY), is("English"));
        assertThat(bundleManager.get("de", SIMPLE_TEST_MESSAGE_KEY), is("Deutsch"));
        
        assertThat(bundleManager.get(Locale.ENGLISH, SIMPLE_TEST_MESSAGE_KEY), is("English"));
        assertThat(bundleManager.get(Locale.GERMAN, SIMPLE_TEST_MESSAGE_KEY), is("Deutsch"));
    }
    
    @Test
    public void testGettingAMessageWithParameters() {
        assertThat(bundleManager.get("default", TEST_MESSAGE_WITH_PARAMETERS, "Param0", "Param1"), is("English Param0 - Param1"));
        assertThat(bundleManager.get("en", TEST_MESSAGE_WITH_PARAMETERS, "Param0", "Param1"), is("English Param0 - Param1"));
        assertThat(bundleManager.get("de", TEST_MESSAGE_WITH_PARAMETERS, "Param0", "Param1"), is("Deutsch Param0 - Param1"));
        
        assertThat(bundleManager.get(Locale.ENGLISH, TEST_MESSAGE_WITH_PARAMETERS, "Param0", "Param1"), is("English Param0 - Param1"));
        assertThat(bundleManager.get(Locale.GERMAN, TEST_MESSAGE_WITH_PARAMETERS, "Param0", "Param1"), is("Deutsch Param0 - Param1"));
    }

}
