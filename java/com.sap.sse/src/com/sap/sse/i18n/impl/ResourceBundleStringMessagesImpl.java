package com.sap.sse.i18n.impl;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import com.sap.sse.i18n.ResourceBundleStringMessages;

public class ResourceBundleStringMessagesImpl implements ResourceBundleStringMessages {
    
    private static final String MESSAGE_PARAMETER_START = "{";
    private static final String MESSAGE_PARAMETER_END = "}";
    
    private final String resourceBaseName;
    private final ClassLoader resourceClassLoader;
    private String encoding;
    
    public ResourceBundleStringMessagesImpl(String resourceBaseName, ClassLoader resourceClassLoader, String encoding) {
        this.resourceBaseName = resourceBaseName;
        this.resourceClassLoader = resourceClassLoader;
        this.encoding = encoding;
    }

    public ResourceBundleStringMessagesImpl(String resourceBaseName, ClassLoader resourceClassLoader) {
        this.resourceBaseName = resourceBaseName;
        this.resourceClassLoader = resourceClassLoader;
        this.encoding = StandardCharsets.ISO_8859_1.name();
    }
    
    @Override
    public String get(Locale locale, String messageKey) {
        return get(locale, messageKey, new String[0]);
    }
    
    @Override
    public String get(Locale locale, String messageKey, String... parameters) {
        String result = getResourceBundle(locale).getString(messageKey);
        for (int i = 0; i < parameters.length; i++) {
            String target = MESSAGE_PARAMETER_START + i + MESSAGE_PARAMETER_END;
            result = result.replace(target, parameters[i]);
        }
        return result;
    }

    private ResourceBundle getResourceBundle(Locale locale) {
        Control controller = Util.createControl(encoding);
        if (resourceClassLoader != null) {
            return ResourceBundle.getBundle(resourceBaseName, locale, resourceClassLoader, controller);
        } else {
            return ResourceBundle.getBundle(resourceBaseName, locale, controller);
        }
    }
    
    @Override
    public String getResourceBaseName() {
        return resourceBaseName;
    }

}
