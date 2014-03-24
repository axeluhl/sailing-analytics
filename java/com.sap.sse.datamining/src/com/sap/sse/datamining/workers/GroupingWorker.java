package com.sap.sse.datamining.workers;

import java.util.Collection;
import java.util.Map;

import com.sap.sse.datamining.shared.GroupKey;

public interface GroupingWorker<DataType> extends ComponentWorker<Map<GroupKey, Collection<DataType>>> {

    public void setDataToGroup(Collection<DataType> data);
    
}
