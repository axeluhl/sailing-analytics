package com.sap.sailing.domain.swisstimingadapter.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.swisstimingadapter.RaceSpecificMessageLoader;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingAdapter;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingAdapterFactory;

public class SwissTimingAdapterFactoryImpl implements SwissTimingAdapterFactory {
    private final Map<DomainFactory, SwissTimingAdapter> adaptersForBaseFactories;
    
    public SwissTimingAdapterFactoryImpl() {
        adaptersForBaseFactories = new HashMap<>();
    }
    
    @Override
    public synchronized SwissTimingAdapter getOrCreateSwissTimingAdapter(DomainFactory baseDomainFactory, RaceSpecificMessageLoader persistence) {
        SwissTimingAdapter result = adaptersForBaseFactories.get(baseDomainFactory);
        if (result == null) {
            result = new SwissTimingAdapterImpl(baseDomainFactory, persistence);
            adaptersForBaseFactories.put(baseDomainFactory, result);
        }
        return result;
    }

}
