package com.sap.sailing.domain.tractracadapter;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.tractracadapter.impl.TracTracAdapterFactoryImpl;

public interface TracTracAdapterFactory {
    /**
     * A default instance to be used only for testing!
     */
    TracTracAdapterFactory INSTANCE = new TracTracAdapterFactoryImpl();
    
    TracTracAdapter getOrCreateTracTracAdapter(DomainFactory baseDomainFactory);
}
