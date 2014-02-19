package com.sap.sse.datamining.impl.workers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.workers.GroupingWorker;

public abstract class AbstractGroupingWorker<DataType> extends AbstractComponentWorker<Map<GroupKey, Collection<DataType>>>
                                                       implements GroupingWorker<DataType> {

    private Collection<DataType> data;

    @Override
    public void setDataToGroup(Collection<DataType> data) {
        this.data = data;
    }
    
    @Override
    protected Map<GroupKey, Collection<DataType>> doWork() {
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
