package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.datamining.GroupKey;
import com.sap.sailing.datamining.Grouper;

public abstract class AbstractGrouper<DataType> implements Grouper<DataType> {
    
    @Override
    public Map<GroupKey, Collection<DataType>> group(Collection<DataType> data) {
        Map<GroupKey, Collection<DataType>> groupedData = new HashMap<GroupKey, Collection<DataType>>();
        for (DataType dataEntry : data) {
            GroupKey key = getGroupKeyFor(dataEntry);
            if (key != null) {
                if (!groupedData.containsKey(key)) {
                    groupedData.put(key, new ArrayList<DataType>());
                }
                groupedData.get(key).add(dataEntry);
            }
        }
        return groupedData;
    }

    protected abstract GroupKey getGroupKeyFor(DataType dataEntry);

}
