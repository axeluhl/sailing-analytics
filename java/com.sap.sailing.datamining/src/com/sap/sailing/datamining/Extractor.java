package com.sap.sailing.datamining;

import java.util.Collection;

import com.sap.sailing.datamining.shared.Unit;

public interface Extractor<DataType, ExtractedType> {

    public String getSignifier();
	public Unit getUnit();
	public int getValueDecimals();

    public Collection<ExtractedType> extract(Collection<DataType> dataEntry);
    public ExtractedType extract(DataType dataEntry);

}
