package com.sap.sse.datamining.i18n;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import com.sap.sse.datamining.impl.i18n.CompoundDataMiningStringMessages;
import com.sap.sse.datamining.impl.i18n.DataMiningStringMessagesImpl;

public interface DataMiningStringMessages {

    public String getResourceBaseName();

    public String get(Locale locale, String messageKey);
    public String get(Locale locale, String messageKey, String... parameters);
    
    public static final class Util {

        private static boolean supportedLocalesHaveBeenInitialized = false;
        private static final String DEFAULT_LOCALE_NAME = "default";
        private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
        private static final Map<String, Locale> supportedLocalesMappedByLocaleInfo = new HashMap<>();
        
        private static Map<String, DataMiningStringMessages> stringMessagesMappedByResourceBaseName = new HashMap<>();
        
        public static DataMiningStringMessages getInstanceFor(String resourceBaseName) {
            String key = resourceBaseName;
            if (!stringMessagesMappedByResourceBaseName.containsKey(key)) {
                stringMessagesMappedByResourceBaseName.put(key, new DataMiningStringMessagesImpl(resourceBaseName));
            }
            return stringMessagesMappedByResourceBaseName.get(key);
        }
        
        public static DataMiningStringMessages getCompoundStringMessages(String... resourceBaseNames) {
            Collection<DataMiningStringMessages> stringMessages = new HashSet<>();
            for (String resourceBaseName : resourceBaseNames) {
                stringMessages.add(getInstanceFor(resourceBaseName));
            }
            
            return new CompoundDataMiningStringMessages(stringMessages);
        }

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
