package com.sap.sse.security.datamining.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.security.datamining.data.HasPreferenceOfUserContext;
import com.sap.sse.security.datamining.data.HasUserContext;
import com.sap.sse.security.datamining.data.impl.PreferenceOfUserWithContext;

public class SecurityPreferencesOfUserRetrievalProcessor extends AbstractSecurityPreferencesOfUserRetrievalProcessor<HasUserContext, HasPreferenceOfUserContext> {
    public SecurityPreferencesOfUserRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasPreferenceOfUserContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(executor, HasUserContext.class, HasPreferenceOfUserContext.class, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected HasPreferenceOfUserContext createPreferenceWithContext(HasUserContext element, String preferenceName, String preferenceValue) {
        return new PreferenceOfUserWithContext(element, preferenceName, preferenceValue);
    }
}
