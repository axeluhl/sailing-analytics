package com.sap.sse.datamining.shared.impl.dto.parameters;

import java.io.Serializable;
import java.util.HashSet;

import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public class ValueListFilterParameter extends AbstractParameterizedDimensionFilter {
    private static final long serialVersionUID = -8440835683986197499L;
    
    private HashSet<? extends Serializable> values;

    public  ValueListFilterParameter() { }

    public ValueListFilterParameter(DataRetrieverLevelDTO retriverLevel, FunctionDTO dimension, HashSet<? extends Serializable> values) {
        super(retriverLevel, dimension);
        this.values = new HashSet<>(values);
    }
    
    @Override
    public HashSet<? extends Serializable> getValues() {
        return new HashSet<>(values);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((values == null) ? 0 : values.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ValueListFilterParameter other = (ValueListFilterParameter) obj;
        if (values == null) {
            if (other.values != null)
                return false;
        } else if (!values.equals(other.values))
            return false;
        return true;
    }

}
