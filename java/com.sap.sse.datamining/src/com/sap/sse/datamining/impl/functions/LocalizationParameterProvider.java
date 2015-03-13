package com.sap.sse.datamining.impl.functions;

import java.util.Locale;

import com.sap.sse.i18n.ResourceBundleStringMessages;

public class LocalizationParameterProvider extends SimpleParameterProvider {
    
    public LocalizationParameterProvider(Locale locale, ResourceBundleStringMessages stringMessages) {
        super(new Object[] {locale, stringMessages});
    }

}
