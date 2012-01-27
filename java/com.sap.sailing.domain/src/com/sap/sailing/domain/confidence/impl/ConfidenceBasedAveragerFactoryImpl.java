package com.sap.sailing.domain.confidence.impl;

import com.sap.sailing.domain.confidence.ConfidenceBasedAverager;
import com.sap.sailing.domain.confidence.ConfidenceBasedAveragerFactory;

public class ConfidenceBasedAveragerFactoryImpl implements ConfidenceBasedAveragerFactory {
    @Override
    public <ValueType, BaseType, RelativeTo> ConfidenceBasedAverager<ValueType, BaseType, RelativeTo> createAverager() {
        return new ConfidenceBasedAveragerImpl<ValueType, BaseType, RelativeTo>();
    }
}
