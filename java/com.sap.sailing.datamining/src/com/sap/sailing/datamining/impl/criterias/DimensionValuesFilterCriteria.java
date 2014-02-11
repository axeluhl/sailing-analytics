package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;
import java.util.HashSet;

import com.sap.sailing.datamining.Dimension;
import com.sap.sse.datamining.components.FilterCriteria;

public class DimensionValuesFilterCriteria<DataType, ValueType> implements FilterCriteria<DataType> {
    
    private Dimension<DataType, ValueType> dimension;
    private Collection<ValueType> values;

    public DimensionValuesFilterCriteria(Dimension<DataType, ValueType> dimension, Collection<ValueType> values) {
        this.dimension = dimension;
        this.values = new HashSet<ValueType>(values);
    }

    @Override
    public boolean matches(DataType data) {
        ValueType dataValue = dimension.getDimensionValueFrom(data);
        if (dataValue == null) {
            return false;
        }
        
        for (ValueType value : values) {
            if (value.equals(dataValue)) {
                return true;
            }
        }
        return false;
    }

}
