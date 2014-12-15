package com.sap.sse.datamining.impl.functions;

import java.util.Locale;

import com.sap.sse.datamining.functions.ParameterProvider;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;

public class StringMessagesParameterProvider implements ParameterProvider {
    
    private final Object[] parameters;
    
    public StringMessagesParameterProvider(Locale locale, DataMiningStringMessages stringMessages) {
        parameters = new Object[] {locale, stringMessages};
    }
    
    @Override
    public Object[] getParameters() {
        return parameters;
    }

}
