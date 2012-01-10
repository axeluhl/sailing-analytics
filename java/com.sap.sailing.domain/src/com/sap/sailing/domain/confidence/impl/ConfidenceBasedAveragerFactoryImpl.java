package com.sap.sailing.domain.confidence.impl;

import com.sap.sailing.domain.confidence.ConfidenceBasedAverager;
import com.sap.sailing.domain.confidence.ConfidenceBasedAveragerFactory;

public class ConfidenceBasedAveragerFactoryImpl implements ConfidenceBasedAveragerFactory {
    @Override
    public <ValueType, AveragesTo> ConfidenceBasedAverager<ValueType, AveragesTo> createAverager() {
        return new ConfidenceBasedAveragerImpl<ValueType, AveragesTo>();
    }
}
