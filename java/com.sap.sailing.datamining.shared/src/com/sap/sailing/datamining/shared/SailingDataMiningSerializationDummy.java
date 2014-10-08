package com.sap.sailing.datamining.shared;

import java.io.Serializable;

import com.sap.sailing.domain.common.LegType;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;

@SuppressWarnings("unused")
public final class SailingDataMiningSerializationDummy implements Serializable {
    private static final long serialVersionUID = 2L;
    
    private LegType legType;
    
    private SailingDataMiningSerializationDummy() { }
    
}
