package com.sap.sailing.datamining;

import java.util.Collection;
import java.util.Map;

import com.sap.sse.datamining.shared.GroupKey;

public interface ExtractionWorker<DataType, ExtractedType> extends ComponentWorker<Map<GroupKey, Collection<ExtractedType>>> {

    public void setDataToExtractFrom(Map<GroupKey, Collection<DataType>> data);

}
