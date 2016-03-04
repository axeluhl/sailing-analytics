package com.sap.sailing.gwt.common.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.i18n.client.LocaleInfo;
import com.sap.sse.common.Util;

public final class GWTLocaleUtil {
    private static final String NO_LOCALE_TEXT = "---";
    
    private GWTLocaleUtil() {
    }
    
    public static Iterable<String> getAvailableLocales() {
        List<String> result = new ArrayList<>();
        result.addAll(Arrays.asList(LocaleInfo.getAvailableLocaleNames()));
        result.remove("default");
        return result;
    }
    
    public static Iterable<String> getAvailableLocalesAndDefault() {
        List<String> result = new ArrayList<>();
        result.add(null);
        Util.addAll(getAvailableLocales(), result);
        return result;
    }
    
    public static int getLanguageCountWithDefault() {
        return Util.size(getAvailableLocalesAndDefault());
    }

    public static String getDecoratedLanguageDisplayNameWithDefaultLocaleSupport(String localeNameOrNull) {
        if (localeNameOrNull == null || localeNameOrNull.isEmpty()) {
            return NO_LOCALE_TEXT;
        }
        return LocaleInfo.getLocaleNativeDisplayName(localeNameOrNull) + " ["+localeNameOrNull+"]";
    }
}
