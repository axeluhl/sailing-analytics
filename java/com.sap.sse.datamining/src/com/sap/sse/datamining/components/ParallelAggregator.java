package com.sap.sse.datamining.components;

import java.util.Collection;
import java.util.Map;

import com.sap.sse.datamining.shared.GroupKey;

public interface ParallelAggregator<ExtractedType, AggregatedType> extends ParallelComponent<Map<GroupKey, Collection<ExtractedType>>,
                                                                                             Map<GroupKey, AggregatedType>> {

    public String getName();

}
