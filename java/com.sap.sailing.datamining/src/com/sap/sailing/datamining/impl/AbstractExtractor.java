package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.Extractor;
import com.sap.sailing.datamining.shared.Unit;

public abstract class AbstractExtractor<DataType, ExtractedType> implements Extractor<DataType, ExtractedType> {
    
    private String signifier;
	private Unit unit;
	private int valueDecimals;

    public AbstractExtractor(String signifier, Unit unit, int valueDecimals) {
        this.signifier = signifier;
        this.unit = unit;
        this.valueDecimals = valueDecimals;
    }
    
    @Override
    public String getSignifier() {
        return signifier;
    }
    
    @Override
    public Unit getUnit() {
    	return unit;
    }
    
    @Override
    public int getValueDecimals() {
    	return valueDecimals;
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
