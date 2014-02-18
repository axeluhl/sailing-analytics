package com.sap.sse.datamining.test.util;

import java.util.Locale;

import com.sap.sse.datamining.impl.i18n.DataMiningResourceBundleManager;

public class OpenResourceBundleManager extends DataMiningResourceBundleManager {

    public OpenResourceBundleManager(Locale defaultLocale) {
        super(defaultLocale);
    }
    
    public String get(Locale locale, String message, String... parameters) {
        return super.get(locale, message, parameters);
    }
    
    public String get(Locale locale, String message) {
        return super.get(locale, message, new String[0]);
    }
    
    public String get(String localeName, String message, String... parameters) {
        return this.get(getLocaleFrom(localeName), message, parameters);
    }
    
    public String get(String localeName, String message) {
        return this.get(getLocaleFrom(localeName), message);
    }
    
}