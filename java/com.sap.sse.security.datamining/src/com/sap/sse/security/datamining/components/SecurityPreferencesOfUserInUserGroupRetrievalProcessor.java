package com.sap.sse.security.datamining.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.security.datamining.data.HasPreferenceOfUserInUserGroupContext;
import com.sap.sse.security.datamining.data.HasUserInUserGroupContext;
import com.sap.sse.security.datamining.data.impl.PreferenceOfUserInUserGroupWithContext;

public class SecurityPreferencesOfUserInUserGroupRetrievalProcessor
extends AbstractSecurityPreferencesOfUserRetrievalProcessor<HasUserInUserGroupContext, HasPreferenceOfUserInUserGroupContext> {
    public SecurityPreferencesOfUserInUserGroupRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasPreferenceOfUserInUserGroupContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(executor, HasUserInUserGroupContext.class, HasPreferenceOfUserInUserGroupContext.class, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected HasPreferenceOfUserInUserGroupContext createPreferenceWithContext(HasUserInUserGroupContext element, String preferenceName, String preferenceValue) {
        return new PreferenceOfUserInUserGroupWithContext(element, preferenceName, preferenceValue);
    }
}
