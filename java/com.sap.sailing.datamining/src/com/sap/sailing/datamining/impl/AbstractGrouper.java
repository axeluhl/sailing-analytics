package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.datamining.GroupingWorker;
import com.sap.sailing.datamining.WorkReceiver;
import com.sap.sailing.datamining.shared.GroupKey;

public abstract class AbstractGrouper<DataType> implements GroupingWorker<DataType> {

    private WorkReceiver<Map<GroupKey, Collection<DataType>>> receiver;
    private boolean isDone;
    private Collection<DataType> data;

    @Override
    public void setReceiver(WorkReceiver<Map<GroupKey, Collection<DataType>>> receiver) {
        this.receiver = receiver;
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public void run() {
        receiver.receiveWork(group(data));
        isDone = true;
    }

    @Override
    public void setDataToGroup(Collection<DataType> data) {
        this.data = data;
    }
    
    private Map<GroupKey, Collection<DataType>> group(Collection<DataType> data) {
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
