package com.sap.sse.i18n.impl;

import java.util.Locale;

import com.sap.sse.i18n.ResourceBundleStringMessages;

/**
 * Null pattern for {@link ResourceBundleStringMessages}.<br>
 * Returns the following values:
 * <ul>
 *   <li>{@link #getResourceBaseName()}: An empty String
 *   <li>{@link #get(Locale, String)}: The <code>messageKey</code>
 *   <li>{@link #get(Locale, String, String...)}: <code>messageKey(parameter1, parameter2, ...)</code>
 * </ul>
 * 
 * @author Lennart Hensler (D054527)
 *
 */
public class NullResourceBundleStringMessages implements ResourceBundleStringMessages {

    /**
     * @return An empty String.
     */
    @Override
    public String getResourceBaseName() {
        return "";
    }

    /**
     * @return The <code>messageKey</code>.
     */
    @Override
    public String get(Locale locale, String messageKey) {
        return messageKey;
    }

    /**
     * @return <code>messageKey(parameter1, parameter2, ...)</code>
     */
    @Override
    public String get(Locale locale, String messageKey, String... parameters) {
        StringBuilder b = new StringBuilder(messageKey + "(");
        boolean first = true;
        for (String parameter : parameters) {
            if (!first) {
                b.append(", ");
            }
            b.append(parameter);
            first = false;
        }
        b.append(")");
        return b.toString();
    }

}
