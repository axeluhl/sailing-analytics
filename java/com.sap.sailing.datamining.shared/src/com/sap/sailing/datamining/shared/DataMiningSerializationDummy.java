package com.sap.sailing.datamining.shared;

import java.io.Serializable;

import com.sap.sailing.datamining.shared.impl.GenericGroupKey;

@SuppressWarnings("unused")
public final class DataMiningSerializationDummy implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private GenericGroupKey<String> groupKey;
    
    private DataMiningSerializationDummy() { }
    
}
