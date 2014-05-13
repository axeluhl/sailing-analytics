package com.sap.sse.datamining.components;

import java.util.Collection;
import java.util.Map;

import com.sap.sse.datamining.shared.GroupKey;

public interface ParallelGrouper<DataType> extends ParallelComponent<Collection<DataType>, Map<GroupKey, Collection<DataType>>> {

}
