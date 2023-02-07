package com.sap.sse.datamining.shared.impl.dto.parameters;

import java.io.Serializable;
import java.util.HashSet;

public class ValueListFilterParameter extends AbstractParameterizedDimensionFilter {
    private static final long serialVersionUID = -8440835683986197499L;
    
    private HashSet<? extends Serializable> values;

    @Deprecated // GWT serialization only
    ValueListFilterParameter() { }

    public ValueListFilterParameter(String name, String typeName, HashSet<? extends Serializable> values) {
        super(name, typeName);
        this.values = new HashSet<>(values);
    }
    
    @Override
    public HashSet<? extends Serializable> getValues() {
        return new HashSet<>(values);
    }
}
