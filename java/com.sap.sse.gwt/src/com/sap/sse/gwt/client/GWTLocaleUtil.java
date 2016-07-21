package com.sap.sse.gwt.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gwt.i18n.client.LocaleInfo;
import com.sap.sse.common.Util;

/**
 * Utilities for working with GWT client locales.
 */
public final class GWTLocaleUtil {
    private static final String NO_LOCALE_TEXT = "---";
    
    private GWTLocaleUtil() {
    }
    
    /**
     * Returns all available client locales. As GWT works with a placeholder locale "default", this ensures that only
     * real locales are returned.
     * 
     * @return all available client locales
     */
    public static Iterable<String> getAvailableLocales() {
        List<String> result = new ArrayList<>();
        result.addAll(Arrays.asList(LocaleInfo.getAvailableLocaleNames()));
        result.remove("default");
        return result;
    }
    
    /**
     * Returns all available client locales plus a null entry. This is useful where you need to populate UI elements for
     * all locales plus a fallback entry.
     * 
     * @return all available client locales plus a null entry
     */
    public static Collection<String> getAvailableLocalesAndDefault() {
        List<String> result = new ArrayList<>();
        result.add(null);
        Util.addAll(getAvailableLocales(), result);
        return result;
    }
    
    /**
     * @return the count of available client locales (including a default entry)
     */
    public static int getLanguageCountWithDefault() {
        return Util.size(getAvailableLocalesAndDefault());
    }

    /**
     * Renders a locale with it's name in the language itself plus the technical locale name.
     * For the fallback entry (null) a placeholder is returned instead
     * 
     * @param localeNameOrNull the locale's name or null for the fallback entry
     * @return a decorated representation of the locale to be shown on the UI
     */
    public static String getDecoratedLanguageDisplayNameWithDefaultLocaleSupport(String localeNameOrNull) {
        if (localeNameOrNull == null || localeNameOrNull.isEmpty()) {
            return NO_LOCALE_TEXT;
        }
        return LocaleInfo.getLocaleNativeDisplayName(localeNameOrNull) + " ["+localeNameOrNull+"]";
    }
}
