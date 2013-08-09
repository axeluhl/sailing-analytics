package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.Extractor;
import com.sap.sailing.domain.base.ScalableInteger;
import com.sap.sailing.domain.confidence.ScalableValue;

public class DataAmountExtractor<DataType> implements Extractor<DataType, Integer, Integer> {

    @Override
    public Collection<ScalableValue<Integer, Integer>> extract(Collection<DataType> data) {
        Collection<ScalableValue<Integer, Integer>> extractedData = new ArrayList<ScalableValue<Integer, Integer>>();
        extractedData.add(new ScalableInteger(data.size()));
        return extractedData;
    }

    public ScalableValue<Integer, Integer> extract(DataType dataEntry) {
        return new ScalableInteger(1);
    }

}
