package com.sap.sse.datamining.impl.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

import com.sap.sse.datamining.i18n.DataMiningStringMessages;

public class DataMiningStringMessagesImpl implements DataMiningStringMessages {
    
    private static final String MESSAGE_PARAMETER_START = "\\{";
    private static final String MESSAGE_PARAMETER_END = "\\}";
    
    private final String resourceBaseName;
    private final ClassLoader resourceClassLoader;
    
    public DataMiningStringMessagesImpl(String resourceBaseName, ClassLoader resourceClassLoader) {
        this.resourceBaseName = resourceBaseName;
        this.resourceClassLoader = resourceClassLoader;
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

    private ResourceBundle getResourceBundle(Locale locale) {
        if (resourceClassLoader != null) {
            return ResourceBundle.getBundle(resourceBaseName, locale, resourceClassLoader);
        } else {
            return ResourceBundle.getBundle(resourceBaseName, locale);
        }
    }
    
    @Override
    public String getResourceBaseName() {
        return resourceBaseName;
    }

}
