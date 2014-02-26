package com.sap.sailing.datamining;

import java.util.Collection;
import java.util.Map;

import com.sap.sailing.datamining.shared.GroupKey;
import com.sap.sailing.datamining.shared.Unit;

public interface ParallelExtractor<DataType, ExtractedType> extends ParallelComponent<Map<GroupKey, Collection<DataType>>,
                                                                                      Map<GroupKey, Collection<ExtractedType>>> {

    public Unit getUnit();

    public int getValueDecimals();

    public String getSignifier();

}
