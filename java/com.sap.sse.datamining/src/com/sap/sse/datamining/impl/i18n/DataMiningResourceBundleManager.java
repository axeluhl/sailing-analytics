package com.sap.sse.datamining.impl.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.shared.Message;

public class DataMiningResourceBundleManager implements DataMiningStringMessages {
    
    private static final String DEFAULT_LOCALE_NAME = "default";
    private static final String STRING_MESSAGES_BASE_NAME = "stringmessages/StringMessages";
    private static final String MESSAGE_PARAMETER_START = "\\{";
    private static final String MESSAGE_PARAMETER_END = "\\}";
    
    private Map<String, Locale> supportedLocalesMappedByLocaleInfo;
    private Map<Locale, ResourceBundle> messagesMappedByLocale;
    
    public DataMiningResourceBundleManager(Locale defaultLocale) {
        supportedLocalesMappedByLocaleInfo = new HashMap<>();
        messagesMappedByLocale = new HashMap<>();
        
        initializeSupportedLocales(defaultLocale);
    }
    
    @Override
    public String get(Locale locale, String messageKey) {
        return get(locale, messageKey, new String[0]);
    }
    
    @Override
    public String get(Locale locale, String messageKey, String... parameters) {
        String result = getResourceBundle(locale).getString(messageKey);
        
        for (int i = 0; i < parameters.length; i++) {
            String replacementRegex = MESSAGE_PARAMETER_START + i + MESSAGE_PARAMETER_END;
            result = result.replaceAll(replacementRegex, parameters[i]);
        }
        return result;
    }
    
    @Override
    public String get(Locale locale, Message message, String... parameters) {
        return get(locale, message.toString(), parameters);
    }
    
    @Override
    public String get(Locale locale, Message message, Message... parameters) {
        String[] parametersAsString = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parametersAsString[i] = get(locale, parameters[i]);
        }
        return get(locale, message, parametersAsString);
    }
    
    @Override
    public String get(Locale locale, Message message) {
        return get(locale, message, new String[0]);
    }

    @Override
    public Locale getLocaleFrom(String localeName) {
        Locale locale = supportedLocalesMappedByLocaleInfo.get(localeName);
        return locale != null ? locale : supportedLocalesMappedByLocaleInfo.get(DEFAULT_LOCALE_NAME);
    }
    
    private ResourceBundle getResourceBundle(Locale locale) {
        if (!messagesMappedByLocale.containsKey(locale)) {
            messagesMappedByLocale.put(locale, ResourceBundle.getBundle(STRING_MESSAGES_BASE_NAME, locale));
        }
        return messagesMappedByLocale.get(locale);
    }

    private void initializeSupportedLocales(Locale defaultLocale) {
        supportedLocalesMappedByLocaleInfo.put(DEFAULT_LOCALE_NAME, defaultLocale);
        supportedLocalesMappedByLocaleInfo.put("en", Locale.ENGLISH);
        supportedLocalesMappedByLocaleInfo.put("de", Locale.GERMAN);
    }

}
