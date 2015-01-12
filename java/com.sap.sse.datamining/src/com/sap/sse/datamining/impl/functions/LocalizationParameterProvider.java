package com.sap.sse.datamining.impl.functions;

import java.util.Locale;

import com.sap.sse.datamining.i18n.DataMiningStringMessages;

public class LocalizationParameterProvider extends SimpleParameterProvider {
    
    public LocalizationParameterProvider(Locale locale, DataMiningStringMessages stringMessages) {
        super(new Object[] {locale, stringMessages});
    }

}
