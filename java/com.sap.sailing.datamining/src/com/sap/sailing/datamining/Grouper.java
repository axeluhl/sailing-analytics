package com.sap.sailing.datamining;

import java.util.Collection;
import java.util.Map;

public interface Grouper<DataType> {

    public Map<GroupKey, Collection<DataType>> group(Collection<DataType> data);
    
}
