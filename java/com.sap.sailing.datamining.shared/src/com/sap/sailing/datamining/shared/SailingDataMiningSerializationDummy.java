package com.sap.sailing.datamining.shared;

import com.sap.sailing.datamining.shared.dto.DistanceDTO;
import com.sap.sailing.domain.common.LegType;
import com.sap.sse.datamining.shared.SerializationDummy;

@SuppressWarnings("unused")
public final class SailingDataMiningSerializationDummy implements SerializationDummy {
    private static final long serialVersionUID = 2L;
    
    private LegType legType;
    private DistanceDTO distance;
    
    private SailingDataMiningSerializationDummy() { }
    
}
