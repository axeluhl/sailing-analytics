package com.sap.sse.datamining.factories;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.CompoundGroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;

public class GroupKeyFactory {

    public static <DataType> GroupKey createNestingCompoundKeyFor(DataType input, Iterable<Function<?>> dimensions) {
        Iterator<Function<?>> dimensionsIterator = dimensions.iterator();
        
        Function<?> mainDimension = dimensionsIterator.next();
        GroupKey mainKey = createDimensionValueGroupKeyFor(input, mainDimension);
        List<GroupKey> subKeys = new ArrayList<>();
        if (dimensionsIterator.hasNext()) {
            while (dimensionsIterator.hasNext()) {
                Function<?> dimension = dimensionsIterator.next();
                subKeys.add(createDimensionValueGroupKeyFor(input, dimension));
            }
        }
        return subKeys.isEmpty() ? mainKey : new CompoundGroupKey(mainKey, subKeys);
    }

    public static <DataType> GroupKey createDimensionValueGroupKeyFor(DataType input, Function<?> mainDimension) {
        Object keyValue = mainDimension.tryToInvoke(input);
        return new GenericGroupKey<Object>(keyValue);
    }

}
