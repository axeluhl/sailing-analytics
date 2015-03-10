package com.sap.sse.datamining.impl.i18n;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.test.util.TestsUtil;
import com.sap.sse.i18n.ResourceBundleStringMessages;
import com.sap.sse.i18n.impl.CompoundResourceBundleStringMessages;

public class TestI18N {

    private static final String SIMPLE_TEST_MESSAGE_KEY = "SimpleTestMessage";
    private static final String TEST_MESSAGE_WITH_PARAMETERS = "TestMessageWithParameters";
    
    private ResourceBundleStringMessages testStringMessages;
    
    @Before
    public void initializeBundleManager() {
        testStringMessages = TestsUtil.getTestStringMessages();
    }

    @Test
    public void testGettingASimpleMessage() {
        assertThat(testStringMessages.get(Locale.ENGLISH, SIMPLE_TEST_MESSAGE_KEY), is("English"));
        assertThat(testStringMessages.get(Locale.GERMAN, SIMPLE_TEST_MESSAGE_KEY), is("Deutsch"));
    }
    
    @Test
    public void testGettingAMessageWithParameters() {
        assertThat(testStringMessages.get(Locale.ENGLISH, TEST_MESSAGE_WITH_PARAMETERS, "Param0", "Param1"), is("English Param0 - Param1"));
        assertThat(testStringMessages.get(Locale.GERMAN, TEST_MESSAGE_WITH_PARAMETERS, "Param0", "Param1"), is("Deutsch Param0 - Param1"));
    }
    
    @Test
    public void testDynamicCompoundStringMessages() {
        CompoundResourceBundleStringMessages compoundStringMessages = new CompoundResourceBundleStringMessages();
        
        try {
            compoundStringMessages.get(Locale.ENGLISH, SIMPLE_TEST_MESSAGE_KEY);
            fail("There shouldn't be a string message in an empty compound string messages");
        } catch (MissingResourceException e) { }
        
        compoundStringMessages.addStringMessages(testStringMessages);
        assertThat(testStringMessages.get(Locale.ENGLISH, SIMPLE_TEST_MESSAGE_KEY), is("English"));
        assertThat(testStringMessages.get(Locale.GERMAN, SIMPLE_TEST_MESSAGE_KEY), is("Deutsch"));
        
        compoundStringMessages.removeStringMessages(testStringMessages);
        try {
            compoundStringMessages.get(Locale.ENGLISH, SIMPLE_TEST_MESSAGE_KEY);
            fail("There shouldn't be a string message in an empty compound string messages");
        } catch (MissingResourceException e) { }
    }
    
    @Test
    public void testGetLocaleForLocaleInfoName() {
        assertThat(ResourceBundleStringMessages.Util.getLocaleFor("default"), is(Locale.ENGLISH));
        assertThat(ResourceBundleStringMessages.Util.getLocaleFor("en"), is(Locale.ENGLISH));
        assertThat(ResourceBundleStringMessages.Util.getLocaleFor("de"), is(Locale.GERMAN));

        assertThat(ResourceBundleStringMessages.Util.getLocaleFor("Unsupported locale info name"), is(Locale.ENGLISH));
    }
    
    @Test
    public void testGetSupportedLocales() {
        Set<Locale> supportedLocales = new HashSet<>();
        for (Locale locale : ResourceBundleStringMessages.Util.getSupportedLocales()) {
            supportedLocales.add(locale);
        }
        
        Set<Locale> expectedSupportedLocales = new HashSet<>();
        expectedSupportedLocales.add(Locale.GERMAN);
        expectedSupportedLocales.add(Locale.ENGLISH);
        
        assertThat(supportedLocales, is(expectedSupportedLocales));
    }
    
    @Test(expected=MissingResourceException.class)
    public void testGettingAMessageForAMissingKeyFromSimpleStringMessages() {
        testStringMessages.get(Locale.ENGLISH, "$%&MissingKey/()");
    }
    
    @Test(expected=MissingResourceException.class)
    public void testGettingAMessageForAMissingKeyFromCompoundStringMessages() {
        TestsUtil.getTestStringMessagesWithProductiveMessages().get(Locale.ENGLISH, "$%&MissingKey/()");
    }

}
