package com.sap.sailing.domain.common.orc.impl;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.common.orc.ORCCertificateSelection;

public class ORCCertificateSelectionImpl implements ORCCertificateSelection {
    private Map<Serializable, String> certificateIdsForBoatIds;
    
    public ORCCertificateSelectionImpl(Map<Serializable, String> certificateIdsForBoatIds) {
        this.certificateIdsForBoatIds = certificateIdsForBoatIds;
    }
    
    @Override
    public Iterable<Entry<Serializable, String>> getCertificateIdsForBoatIds() {
        return certificateIdsForBoatIds.entrySet();
    }
}
