package com.sap.sse.datamining.factories;

import java.util.Iterator;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.NestingCompoundGroupKey;

public class GroupKeyFactory {

    public static <DataType> GroupKey createNestingCompoundKeyFor(DataType input, Iterator<Function<?>> dimensionsIterator) {
        Function<?> mainDimension = dimensionsIterator.next();
        GroupKey key = createDimensionValueGroupKeyFor(input, mainDimension);
        if (dimensionsIterator.hasNext()) {
            key = new NestingCompoundGroupKey(key, createNestingCompoundKeyFor(input, dimensionsIterator));
        }
        return key;
    }

    public static <DataType> GroupKey createDimensionValueGroupKeyFor(DataType input, Function<?> mainDimension) {
        Object keyValue = mainDimension.tryToInvoke(input);
        return new GenericGroupKey<Object>(keyValue);
    }

}
