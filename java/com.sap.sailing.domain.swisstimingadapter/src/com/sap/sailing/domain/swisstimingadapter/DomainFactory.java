package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.domain.swisstimingadapter.impl.DomainFactoryImpl;

public interface DomainFactory {
    final static DomainFactory INSTANCE = new DomainFactoryImpl();
}
