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
        private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
        private static final Map<String, Locale> supportedLocalesMappedByLocaleInfo = new HashMap<>();

        public static Locale getLocaleFor(String localeInfoName) {
            Locale locale = getSupportedLocalesMap().get(localeInfoName);
            return locale != null ? locale : DEFAULT_LOCALE;
        }
        
        public static Iterable<Locale> getSupportedLocales() {
            return getSupportedLocalesMap().values();
        }
        
        private static Map<String, Locale> getSupportedLocalesMap() {
            if (!supportedLocalesHaveBeenInitialized) {
                initializeSupportedLocales();
            }
            
            return supportedLocalesMappedByLocaleInfo;
        }
        
        private static void initializeSupportedLocales() {
            supportedLocalesMappedByLocaleInfo.put("en", Locale.ENGLISH);
            supportedLocalesMappedByLocaleInfo.put("de", Locale.GERMAN);
            
            supportedLocalesHaveBeenInitialized = true;
        }
        
        private Util () {
        }
        
    }

}
