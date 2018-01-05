package com.sap.sse.i18n.impl;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.sse.i18n.ResourceBundleStringMessages;

public class ResourceBundleStringMessagesImpl implements ResourceBundleStringMessages {
    
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
        final String message = getResourceBundle(locale).getString(messageKey);
        final StringBuilder result = new StringBuilder();
        boolean withinQuotedArea = false;
        for (int i = 0; i < message.length(); i++) {
            if (isSingleQuote(message, i) || (withinQuotedArea && message.charAt(i) == '\'')) {
                withinQuotedArea = !withinQuotedArea;
            } else if (isDoubleQuote(message, i)) {
                result.append('\''); // an escaped single quote
                i++; // skip the second one
            } else {
                if (withinQuotedArea) {
                    result.append(message.charAt(i));
                } else {
                    final int paramNumber = isParameterPlaceholder(message, i);
                    if (paramNumber != -1) {
                        result.append(parameters[paramNumber]);
                        i += ("" + paramNumber).length() + 1; // skip the number plus one curly brace
                    } else {
                        result.append(message.charAt(i));
                    }
                }
            }
        }
        return result.toString();
    }

    private boolean isDoubleQuote(String message, int i) {
        return i < message.length() - 1 && message.charAt(i) == '\'' && message.charAt(i + 1) == '\'';
    }

    private static final Pattern placeholderMatcher = Pattern.compile("\\{([0-9]+)\\}.*$");

    /**
     * @return -1 if there is no placeholder starting at character {@code i} in {@code message}, or the number of the
     *         parameter represented by the placeholder, such as {@code 4} for the placeholder
     * 
     *         <pre>
     *         { 4 }
     *         </pre>
     * 
     *         .
     */
    private int isParameterPlaceholder(String message, int i) {
        final Matcher matcher = placeholderMatcher.matcher(message.substring(i));
        final int result;
        if (matcher.matches()) {
            result = Integer.valueOf(matcher.group(1));
        } else {
            result = -1;
        }
        return result;
    }

    private boolean isSingleQuote(String message, int i) {
        return i < message.length() && message.charAt(i) == '\''
                && (i == message.length() - 1 || message.charAt(i + 1) != '\'');
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
