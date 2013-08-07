package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.Extractor;
import com.sap.sailing.domain.confidence.ScalableValue;

public abstract class AbstractExtractor<DataType, ValueType, AveragesTo> implements Extractor<DataType, ValueType, AveragesTo> {

    @Override
    public Collection<ScalableValue<ValueType, AveragesTo>> extract(Collection<DataType> data) {
        Collection<ScalableValue<ValueType, AveragesTo>> extractedData = new ArrayList<ScalableValue<ValueType,AveragesTo>>();
        for (DataType dataEntry : data) {
            ScalableValue<ValueType, AveragesTo> extractedDataEntry = extract(dataEntry);
            if (extractedDataEntry != null) {
                extractedData.add(extractedDataEntry);
            }
        }
        return extractedData;
    }
    
    protected abstract ScalableValue<ValueType, AveragesTo> extract(DataType dataEntry);

}
