package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.datamining.Grouper;
import com.sap.sailing.datamining.shared.Dimension;

public class GroupByDimension implements Grouper {
    
    private Dimension dimension;

    private GroupByDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    @Override
    public Map<String, Collection<GPSFixWithContext>> group(Collection<GPSFixWithContext> data) {
        Map<String, Collection<GPSFixWithContext>> groupedData = new HashMap<String, Collection<GPSFixWithContext>>();
        for (GPSFixWithContext gpsFix : data) {
            String dimensionValueAsString = gpsFix.getStringRepresentation(dimension);
            if (dimensionValueAsString != null) {
                if (!groupedData.containsKey(dimensionValueAsString)) {
                    groupedData.put(dimensionValueAsString, new ArrayList<GPSFixWithContext>());
                }
                groupedData.get(dimensionValueAsString).add(gpsFix);
            }
        }
        return groupedData;
    }

}
