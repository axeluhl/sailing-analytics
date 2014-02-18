package com.sap.sailing.datamining.test.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.shared.Message;

public class StringMessagesForTests implements DataMiningStringMessages {
    
    private final Map<Locale, Map<String, String>> messages;
    
    public StringMessagesForTests() {
        messages = new HashMap<>();
        initializeEnglishMessages();
        initializeGermanMessages();
    }

    private void initializeEnglishMessages() {
        Map<String, String> englishMessages = new HashMap<>();
        messages.put(Locale.ENGLISH, englishMessages);

        englishMessages.put("dimension", "dimension-english");
        englishMessages.put("value", "sideEffectFreeValue-english");
    }

    private void initializeGermanMessages() {
        Map<String, String> germanMessages = new HashMap<>();
        messages.put(Locale.GERMAN, germanMessages);

        germanMessages.put("dimension", "dimension-deutsch");
        germanMessages.put("value", "sideEffectFreeValue-deutsch");
    }

    @Override
    public Locale getLocaleFrom(String localeName) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String get(Locale locale, Message message) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String get(Locale locale, Message message, String... parameters) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String get(Locale locale, Message message, Message... parameters) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String get(Locale locale, String messageKey) {
        return messages.get(locale).get(messageKey);
    }

    @Override
    public String get(Locale locale, String messageKey, String... parameters) {
        throw new UnsupportedOperationException("Not implemented");
    }
    
}