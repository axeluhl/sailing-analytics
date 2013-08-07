package com.sap.sailing.datamining.impl.criterias;

import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.datamining.shared.Dimension;

public class GPSFixRegexFilterCriteria extends RegexFilterCriteria<GPSFixWithContext> {
    
    private Dimension dimension;

    public GPSFixRegexFilterCriteria(String regex, Dimension dimension) {
        super(regex);
        this.dimension = dimension;
    }

    @Override
    protected String getValueToMatch(GPSFixWithContext data) {
        return data.getStringRepresentation(dimension);
    }

}
