package com.sap.sse.datamining.shared;

import java.io.Serializable;

import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;

@SuppressWarnings("unused")
public final class SSEDataMiningSerializationDummy implements SerializationDummy {
    private static final long serialVersionUID = 1L;
    
    private GenericGroupKey<String> groupKey;
    private ClusterDTO cluster;
    private Number n;
    
    private SSEDataMiningSerializationDummy() { }
    
}
