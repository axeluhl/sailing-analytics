package com.sap.sailing.domain.swisstimingreplayadapter.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayAdapter;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayAdapterFactory;

public class SwissTimingReplayAdapterFactoryImpl implements SwissTimingReplayAdapterFactory {
    private final Map<DomainFactory, SwissTimingReplayAdapter> adaptersForBaseDomainFactories;
    
    public SwissTimingReplayAdapterFactoryImpl() {
        adaptersForBaseDomainFactories = new HashMap<>();
    }

    @Override
    public SwissTimingReplayAdapter createSwissTimingReplayAdapter(DomainFactory baseDomainFactory) {
        SwissTimingReplayAdapter result = adaptersForBaseDomainFactories.get(baseDomainFactory);
        if (result == null) {
            result = new SwissTimingReplayAdapterImpl(baseDomainFactory);
            adaptersForBaseDomainFactories.put(baseDomainFactory, result);
        }
        return result;
    }

}
