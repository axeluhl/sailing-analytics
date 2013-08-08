package com.sap.sailing.datamining.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.shared.GroupKey;

public abstract class GroupByDimension<DataType> extends AbstractGrouper<DataType> {
    
    private Collection<Dimension<DataType>> dimensions;

    public GroupByDimension(Dimension<DataType>... dimensions) {
        this.dimensions = new LinkedHashSet<Dimension<DataType>>(Arrays.asList(dimensions));
    }

    @Override
    protected GroupKey getGroupKeyFor(DataType dataEntry) {
        return createCompoundGroupKey(dataEntry, dimensions.iterator());
    }

    private GroupKey createCompoundGroupKey(DataType dataEntry, Iterator<Dimension<DataType>> iterator) {
        Dimension<DataType> mainDimension = iterator.next();
        if (iterator.hasNext()) {
            return new CompoundGroupKey(createGroupKeyFor(dataEntry, mainDimension), createCompoundGroupKey(dataEntry, iterator));
        } else {
            return createGroupKeyFor(dataEntry, mainDimension);
        }
    }

    protected abstract GroupKey createGroupKeyFor(DataType dataEntry, Dimension<DataType> dimension);

}
