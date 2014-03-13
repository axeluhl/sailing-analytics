package com.sap.sailing.domain.tractracadapter.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.tractracadapter.TracTracAdapter;
import com.sap.sailing.domain.tractracadapter.TracTracAdapterFactory;

public class TracTracAdapterFactoryImpl implements TracTracAdapterFactory {
    private final Map<DomainFactory, TracTracAdapter> adaptersForBaseDomainFactories;
    
    public TracTracAdapterFactoryImpl() {
        adaptersForBaseDomainFactories = new HashMap<>();
    }
    
    @Override
    public synchronized TracTracAdapter getOrCreateTracTracAdapter(DomainFactory baseDomainFactory) {
        TracTracAdapter result = adaptersForBaseDomainFactories.get(baseDomainFactory);
        if (result == null) {
            result = new TracTracAdapterImpl(baseDomainFactory);
            adaptersForBaseDomainFactories.put(baseDomainFactory, result);
        }
        return result;
    }

}
