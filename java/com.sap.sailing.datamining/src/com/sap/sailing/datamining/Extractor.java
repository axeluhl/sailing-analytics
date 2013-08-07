package com.sap.sailing.datamining;

import java.util.Collection;

import com.sap.sailing.domain.confidence.ScalableValue;

public interface Extractor<DataType, ExtractedType, AveragesTo> {

    public Collection<ScalableValue<ExtractedType, AveragesTo>> extract(Collection<DataType> data);

}
