package com.sap.sse.datamining.impl.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.shared.Message;

public class DataMiningStringMessagesImpl implements DataMiningStringMessages {
    
    private static final String MESSAGE_PARAMETER_START = "\\{";
    private static final String MESSAGE_PARAMETER_END = "\\}";
    
    private final String resourceBaseName;
    
    public DataMiningStringMessagesImpl(String resourceBaseName) {
        this.resourceBaseName = resourceBaseName;
    }
    
    @Override
    public String get(Locale locale, String messageKey) {
        return get(locale, messageKey, new String[0]);
    }
    
    @Override
    public String get(Locale locale, Message message) {
        return get(locale, message.toString(), new String[0]);
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
    public String get(Locale locale, String messageKey, String... parameters) {
        String result = getResourceBundle(locale).getString(messageKey);
        
        for (int i = 0; i < parameters.length; i++) {
            String replacementRegex = MESSAGE_PARAMETER_START + i + MESSAGE_PARAMETER_END;
            result = result.replaceAll(replacementRegex, parameters[i]);
        }
        return result;
    }

    private ResourceBundle getResourceBundle(Locale locale) {
        return ResourceBundle.getBundle(resourceBaseName, locale);
    }
    
    @Override
    public String getResourceBaseName() {
        return resourceBaseName;
    }

}
