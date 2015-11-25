package com.sap.sailing.datamining.shared;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.impl.CentralAngleDistance;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sse.datamining.shared.SerializationDummy;

@SuppressWarnings("unused")
public final class SailingDataMiningSerializationDummy implements SerializationDummy {
    private static final long serialVersionUID = 2L;
    
    private LegType legType;
    private Distance distance;
    private CentralAngleDistance centralAngleDistance;
    private MeterDistance meterDistance;
    private NauticalMileDistance nauticalMileDistance;
    
    private SailingDataMiningSerializationDummy() { }
    
}
