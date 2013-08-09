package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.Extractor;

public class DataAmountExtractor<DataType> implements Extractor<DataType, Integer> {

    @Override
    public Collection<Integer> extract(Collection<DataType> data) {
        Collection<Integer> extractedData = new ArrayList<Integer>();
        extractedData.add(data.size());
        return extractedData;
    }

    public Integer extract(DataType dataEntry) {
        return 1;
    }

}
