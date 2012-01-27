package com.sap.sailing.domain.confidence;

import com.sap.sailing.domain.confidence.impl.ConfidenceBasedAveragerFactoryImpl;

public interface ConfidenceBasedAveragerFactory {
    ConfidenceBasedAveragerFactory INSTANCE = new ConfidenceBasedAveragerFactoryImpl();
    
    <ValueType, BaseType, RelativeTo> ConfidenceBasedAverager<ValueType, BaseType, RelativeTo> createAverager();
}
