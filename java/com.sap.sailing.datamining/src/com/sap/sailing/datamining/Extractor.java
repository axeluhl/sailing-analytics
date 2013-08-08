package com.sap.sailing.datamining;

import java.util.Collection;

import com.sap.sailing.domain.confidence.ScalableValue;

public interface Extractor<DataType, ValueType, AveragesTo> {

    public Collection<ScalableValue<ValueType, AveragesTo>> extract(Collection<DataType> data);

    public ScalableValue<ValueType, AveragesTo> extract(DataType dataEntry);

}
