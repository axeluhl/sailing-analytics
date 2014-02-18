package com.sap.sse.datamining.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.sap.sse.datamining.impl.i18n.DataMiningResourceBundleManager;
import com.sap.sse.datamining.shared.Message;

public interface DataMiningStringMessages {

    public Locale getLocaleFrom(String localeName);
    
    public String get(Locale locale, Message message);
    public String get(Locale locale, Message message, String... parameters);
    public String get(Locale locale, Message message, Message... parameters);

    public String get(Locale locale, String messageKey);
    public String get(Locale locale, String messageKey, String... parameters);
    
    //TODO Replace with static method, after Java 8 can be used
    public static final class Util {
        
        private Util () {
        }
        
        private static Map<Locale, DataMiningStringMessages> stringMessagesMappedByLocale = new HashMap<>();
        
        public static DataMiningStringMessages getInstanceFor(Locale locale) {
            if (!stringMessagesMappedByLocale.containsKey(locale)) {
                stringMessagesMappedByLocale.put(locale, new DataMiningResourceBundleManager(locale));
            }
            return stringMessagesMappedByLocale.get(locale);
        }
        
    }

}
