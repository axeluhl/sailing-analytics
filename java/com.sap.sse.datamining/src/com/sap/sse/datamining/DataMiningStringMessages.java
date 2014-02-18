package com.sap.sse.datamining;

import java.util.Locale;

import com.sap.sse.datamining.shared.Message;

public interface DataMiningStringMessages {

    public Locale getLocaleFrom(String localeName);
    
    public String get(Locale locale, Message message);
    public String get(Locale locale, Message message, String... parameters);
    public String get(Locale locale, Message message, Message... parameters);

    public String get(Locale locale, String messageKey);
    public String get(Locale locale, String messageKey, String... parameters);

}
