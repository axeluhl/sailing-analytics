package com.sap.sse.datamining.impl.functions;

import java.util.Collections;
import java.util.Locale;

import com.sap.sse.datamining.functions.ParameterProvider;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public class IdentityFunction extends AbstractFunction<Object> {
    
    public IdentityFunction() {
        super(false);
    }

    @Override
    public Class<?> getDeclaringType() {
        return Object.class;
    }

    @Override
    public Iterable<Class<?>> getParameters() {
        return Collections.emptySet();
    }

    @Override
    public boolean needsLocalizationParameters() {
        return false;
    }

    @Override
    public Class<Object> getReturnType() {
        return Object.class;
    }

    @Override
    public String getSimpleName() {
        return "Identity";
    }

    @Override
    public boolean isLocalizable() {
        return true;
    }

    @Override
    public String getLocalizedName(Locale locale, ResourceBundleStringMessages stringMessages) {
        return stringMessages.get(locale, "Identity");
    }

    @Override
    public Object tryToInvoke(Object instance) {
        return instance;
    }

    @Override
    public Object tryToInvoke(Object instance, ParameterProvider parameterProvider) {
        return instance;
    }

    @Override
    public int getResultDecimals() {
        return 0;
    }

    @Override
    public int getOrdinal() {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public int hashCode() {
        return IdentityFunction.class.getCanonicalName().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!IdentityFunction.class.equals(other.getClass())) {
            return false;
        }
        // Ensure equality, if the classes have been loaded by different class loaders
        return IdentityFunction.class.getCanonicalName().equals(other.getClass().getCanonicalName());
    }

}
