package com.sap.sse.datamining.factories;

import java.util.ArrayList;
import java.util.List;

import com.sap.sse.common.Util;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.CompoundGroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;

public class GroupKeyFactory {

    public static <DataType> GroupKey createNestingCompoundKeyFor(DataType input, Iterable<Function<?>> dimensions) {
        if (Util.size(dimensions) == 1) {
            Function<?> Dimension = Util.get(dimensions, 0);
            return createDimensionValueGroupKeyFor(input, Dimension);
        } else {
            List<GroupKey> keys = new ArrayList<>();
            for (Function<?> Dimension : dimensions) {
                keys.add(createDimensionValueGroupKeyFor(input, Dimension));
            }
            return new CompoundGroupKey(keys);
        }
    }

    public static <DataType> GroupKey createDimensionValueGroupKeyFor(DataType input, Function<?> mainDimension) {
        Object keyValue = mainDimension.tryToInvoke(input);
        return new GenericGroupKey<Object>(keyValue);
    }

}
