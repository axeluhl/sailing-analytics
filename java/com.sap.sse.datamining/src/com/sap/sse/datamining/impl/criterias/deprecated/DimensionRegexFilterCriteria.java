package com.sap.sse.datamining.impl.criterias.deprecated;

import com.sap.sse.datamining.data.Dimension;
import com.sap.sse.datamining.impl.criterias.RegexFilterCriteria;

public class DimensionRegexFilterCriteria<DataType> extends RegexFilterCriteria<DataType> {
    
    private Dimension<DataType, String> dimension;

    public DimensionRegexFilterCriteria(String regex, Dimension<DataType, String> dimension) {
        super(regex);
        this.dimension = dimension;
    }

    @Override
    protected String getValueToMatch(DataType data) {
        return dimension.getDimensionValueFrom(data);
    }

}
