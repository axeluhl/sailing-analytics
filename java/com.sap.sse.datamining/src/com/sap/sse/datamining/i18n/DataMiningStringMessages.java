package com.sap.sse.datamining.i18n;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import com.sap.sse.datamining.impl.i18n.CompoundDataMiningStringMessages;
import com.sap.sse.datamining.impl.i18n.DataMiningStringMessagesImpl;
import com.sap.sse.datamining.shared.Message;

public interface DataMiningStringMessages {

    public String getResourceBaseName();

    public String get(Locale locale, Message message);
    public String get(Locale locale, Message message, String... parameters);
    public String get(Locale locale, Message message, Message... parameters);

    public String get(Locale locale, String messageKey);
    public String get(Locale locale, String messageKey, String... parameters);
    
    //TODO Replace with static method, after Java 8 can be used
    public static final class Util {
        
        private static final String DEFAULT_STRING_MESSAGES_BASE_NAME = "stringmessages/StringMessages";
        
        private static DataMiningStringMessages defaultStringMessages;
        private static Map<String, DataMiningStringMessages> stringMessagesMappedByResourceBaseName = new HashMap<>();
        
        public static String getDefaultStringMessagesBaseName() {
            return DEFAULT_STRING_MESSAGES_BASE_NAME;
        }
        
        public static DataMiningStringMessages getDefaultStringMessages() {
            if (defaultStringMessages == null) {
                defaultStringMessages = new DataMiningStringMessagesImpl(DEFAULT_STRING_MESSAGES_BASE_NAME);
            }
            return defaultStringMessages;
        }
        
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
        
        private Util () {
        }
        
    }

}
