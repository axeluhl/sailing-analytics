package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.shared.Unit;

public class DataAmountExtractor<DataType> extends AbstractExtractor<DataType, Integer> {

    public DataAmountExtractor() {
        super("amount of fixes", Unit.None, 0);
    }

    @Override
    public Collection<Integer> extract(Collection<DataType> data) {
        Collection<Integer> extractedData = new ArrayList<Integer>();
        extractedData.add(data.size());
        return extractedData;
    }

    @Override
    public Integer extract(DataType dataEntry) {
        return 1;
    }

}
