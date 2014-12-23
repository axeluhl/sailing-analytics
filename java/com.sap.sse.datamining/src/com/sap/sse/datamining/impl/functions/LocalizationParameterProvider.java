package com.sap.sse.datamining.impl.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import com.sap.sse.datamining.i18n.DataMiningStringMessages;

public class LocalizationParameterProvider extends SimpleParameterProvider {
    
    public LocalizationParameterProvider(Locale locale, DataMiningStringMessages stringMessages) {
        super(createParameterTypes(), new Object[] {locale, stringMessages});
    }
    
    private static Iterable<Class<?>> createParameterTypes() {
        Collection<Class<?>> parameterTypes = new ArrayList<>();
        parameterTypes.add(Locale.class);
        parameterTypes.add(DataMiningStringMessages.class);
        return parameterTypes;
    }

}
