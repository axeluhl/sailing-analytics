package com.sap.sailing.polars.mining;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;

public class IncrementalRegressionProcessor implements Processor<GroupedDataEntry<GPSFixMovingWithPolarContext>> {

    private final Map<GroupKey, Set<GPSFixMovingWithPolarContext>> container = new HashMap<GroupKey, Set<GPSFixMovingWithPolarContext>>();

    @Override
    public void onElement(GroupedDataEntry<GPSFixMovingWithPolarContext> element) {
        GroupKey key = element.getKey();
        if (!container.containsKey(key)) {
            container.put(key, new HashSet<GPSFixMovingWithPolarContext>());
        }
        Set<GPSFixMovingWithPolarContext> set = container.get(key);
        set.add(element.getDataEntry());
    }

    @Override
    public void finish() throws InterruptedException {
        // Nothing to do here
    }

    @Override
    public void abort() {
        // TODO Auto-generated method stub
    }

    @Override
    public AdditionalResultDataBuilder getAdditionalResultData(AdditionalResultDataBuilder additionalDataBuilder) {
        // TODO Auto-generated method stub
        return null;
    }

}
