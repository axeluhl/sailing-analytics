package com.sap.sse.datamining.factories;

import java.util.Iterator;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.CompoundGroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;

public class GroupKeyFactory {

    public static <DataType> GroupKey createCompoundKeyFor(DataType input, Iterator<Function<?>> dimensionsIterator) {
        Function<?> mainDimension = dimensionsIterator.next();
        GroupKey key = createGroupKeyFor(input, mainDimension);
        if (dimensionsIterator.hasNext()) {
            key = new CompoundGroupKey(key, createCompoundKeyFor(input, dimensionsIterator));
        }
        return key;
    }

    public static <DataType> GroupKey createGroupKeyFor(DataType input, Function<?> mainDimension) {
        Object keyValue = mainDimension.tryToInvoke(input);
        return new GenericGroupKey<Object>(keyValue);
    }

}
