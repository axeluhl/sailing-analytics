package com.sap.sse.security.datamining.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;
import com.sap.sse.security.datamining.data.HasPreferenceContext;
import com.sap.sse.security.datamining.data.HasUserContext;

public abstract class AbstractSecurityPreferencesOfUserRetrievalProcessor<HUG extends HasUserContext, HPC extends HasPreferenceContext>
extends AbstractRetrievalProcessor<HUG, HPC> {
    public AbstractSecurityPreferencesOfUserRetrievalProcessor(ExecutorService executor, Class<HUG> hasUserContextSpecialization,
            Class<HPC> preferenceWithContextSpecialization,
            Collection<Processor<HPC, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(hasUserContextSpecialization, preferenceWithContextSpecialization, executor, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HPC> retrieveData(HUG element) {
        final Set<HPC> data = new HashSet<>();
        for (final Entry<String, String> preferenceNameAndValue : element.getSecurityService().getAllPreferences(element.getUser().getName()).entrySet()) {
            if (isAborted()) {
                break;
            }
            data.add(createPreferenceWithContext(element, preferenceNameAndValue.getKey(), preferenceNameAndValue.getValue()));
        }
        return data;
    }
    
    protected abstract HPC createPreferenceWithContext(HUG userContext, String preferenceName, String preferenceValue);
}
