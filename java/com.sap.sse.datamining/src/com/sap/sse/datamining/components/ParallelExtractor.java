package com.sap.sse.datamining.components;

import java.util.Collection;
import java.util.Map;

import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.Unit;

public interface ParallelExtractor<DataType, ExtractedType> extends ParallelComponent<Map<GroupKey, Collection<DataType>>,
                                                                                      Map<GroupKey, Collection<ExtractedType>>> {

    public Unit getUnit();

    public int getValueDecimals();

    public String getSignifier();

}
