package com.sap.sse.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Allow server-side internationalization similar to GWT client-side by using property files.
 */
public interface ResourceBundleStringMessages {

    public String getResourceBaseName();

    public String get(Locale locale, String messageKey);
    public String get(Locale locale, String messageKey, String... parameters);
    
    public static final class Util {

        private static boolean supportedLocalesHaveBeenInitialized = false;
        private static final String DEFAULT_LOCALE_NAME = "default";
        private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
        private static final Map<String, Locale> supportedLocalesMappedByLocaleInfo = new HashMap<>();

        public static Locale getLocaleFor(String localeInfoName) {
            if (!supportedLocalesHaveBeenInitialized) {
                initializeSupportedLocales();
            }
            
            Locale locale = Util.supportedLocalesMappedByLocaleInfo.get(localeInfoName);
            return locale != null ? locale : DEFAULT_LOCALE;
        }
        
        private static void initializeSupportedLocales() {
            supportedLocalesMappedByLocaleInfo.put(DEFAULT_LOCALE_NAME, DEFAULT_LOCALE);
            supportedLocalesMappedByLocaleInfo.put("en", Locale.ENGLISH);
            supportedLocalesMappedByLocaleInfo.put("de", Locale.GERMAN);
            
            supportedLocalesHaveBeenInitialized = true;
        }
        
        private Util () {
        }
        
    }

}
