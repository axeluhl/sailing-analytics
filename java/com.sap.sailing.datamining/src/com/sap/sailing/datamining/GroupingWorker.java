package com.sap.sailing.datamining;

import java.util.Collection;
import java.util.Map;

import com.sap.sailing.datamining.shared.GroupKey;

public interface GroupingWorker<DataType> extends ComponentWorker<Map<GroupKey, Collection<DataType>>> {

    public void setDataToGroup(Collection<DataType> data);
    
}
