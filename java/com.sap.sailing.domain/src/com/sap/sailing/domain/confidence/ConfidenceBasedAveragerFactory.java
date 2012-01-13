package com.sap.sailing.domain.confidence;

import com.sap.sailing.domain.confidence.impl.ConfidenceBasedAveragerFactoryImpl;

public interface ConfidenceBasedAveragerFactory {
    ConfidenceBasedAveragerFactory INSTANCE = new ConfidenceBasedAveragerFactoryImpl();
    
    <ValueType, AveragesTo> ConfidenceBasedAverager<ValueType, AveragesTo> createAverager();
}
