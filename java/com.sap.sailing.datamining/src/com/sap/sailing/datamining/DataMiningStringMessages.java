package com.sap.sailing.datamining;

import java.util.Locale;

import com.sap.sailing.datamining.shared.Message;

public interface DataMiningStringMessages {

    Locale getLocaleFrom(String localeName);
    
    public String get(String localeName, Message message);
    public String get(String localeName, Message message, String... parameters);
    public String get(String localeName, Message message, Message... parameters);
    
    public String get(Locale locale, Message message);
    public String get(Locale locale, Message message, String... parameters);
    public String get(Locale locale, Message message, Message... parameters);

}
