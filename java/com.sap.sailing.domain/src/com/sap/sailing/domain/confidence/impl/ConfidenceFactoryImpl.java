package com.sap.sailing.domain.confidence.impl;

import com.sap.sailing.domain.common.confidence.Weigher;
import com.sap.sailing.domain.confidence.ConfidenceBasedWindAverager;
import com.sap.sailing.domain.confidence.ConfidenceFactory;

public class ConfidenceFactoryImpl extends
        com.sap.sailing.domain.common.confidence.impl.ConfidenceBasedAveragerFactoryImpl implements ConfidenceFactory {
    @Override
    public <RelativeTo> ConfidenceBasedWindAverager<RelativeTo> createWindAverager(Weigher<RelativeTo> weigher) {
        return new ConfidenceBasedWindAveragerImpl<RelativeTo>(weigher);
    }
}
