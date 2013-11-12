package com.sap.sailing.domain.tractracadapter;

import com.sap.sailing.domain.base.DomainFactory;

public interface TracTracAdapterFactory {
    TracTracAdapter getOrCreateTracTracAdapter(DomainFactory baseDomainFactory);
}
