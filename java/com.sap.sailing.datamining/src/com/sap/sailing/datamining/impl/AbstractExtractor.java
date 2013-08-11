package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.Extractor;

public abstract class AbstractExtractor<DataType, ExtractedType> implements Extractor<DataType, ExtractedType> {
    
    private String signifier;

    public AbstractExtractor(String signifier) {
        this.signifier = signifier;
    }
    
    @Override
    public String getSignifier() {
        return signifier;
    }

    @Override
    public Collection<ExtractedType> extract(Collection<DataType> data) {
        Collection<ExtractedType> extractedData = new ArrayList<ExtractedType>();
        for (DataType dataEntry : data) {
            ExtractedType extractedDataEntry = extract(dataEntry);
            if (extractedDataEntry != null) {
                extractedData.add(extractedDataEntry);
            }
        }
        return extractedData;
    }

}
