package com.sap.sse.datamining.impl.workers;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import com.sap.sse.datamining.data.deprecated.Dimension;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.CompoundGroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;

public class MultiDimensionalGroupingWorker<DataType, ValueType> extends AbstractGroupingWorker<DataType> {
    
    private Collection<Dimension<DataType, ValueType>> dimensions;

    public MultiDimensionalGroupingWorker(Collection<Dimension<DataType, ValueType>> dimensions) {
        this.dimensions = new LinkedHashSet<Dimension<DataType, ValueType>>(dimensions);
    }

    @Override
    protected GroupKey getGroupKeyFor(DataType dataEntry) {
        return createCompoundGroupKey(dataEntry, dimensions.iterator());
    }

    private GroupKey createCompoundGroupKey(DataType dataEntry, Iterator<Dimension<DataType, ValueType>> iterator) {
        Dimension<DataType, ValueType> mainDimension = iterator.next();
        if (iterator.hasNext()) {
            return new CompoundGroupKey(createGroupKeyFor(dataEntry, mainDimension), createCompoundGroupKey(dataEntry, iterator));
        } else {
            return createGroupKeyFor(dataEntry, mainDimension);
        }
    }

    protected GroupKey createGroupKeyFor(DataType dataEntry, Dimension<DataType, ValueType> dimension){
        return new GenericGroupKey<ValueType>(dimension.getDimensionValueFrom(dataEntry));
    };

}
