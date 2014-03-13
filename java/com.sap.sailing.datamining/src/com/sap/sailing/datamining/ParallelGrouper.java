package com.sap.sailing.datamining;

import java.util.Collection;
import java.util.Map;

import com.sap.sailing.datamining.shared.GroupKey;

public interface ParallelGrouper<DataType> extends ParallelComponent<Collection<DataType>, Map<GroupKey, Collection<DataType>>> {

}
