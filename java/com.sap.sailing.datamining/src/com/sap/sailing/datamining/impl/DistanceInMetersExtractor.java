package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.datamining.Extractor;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public class DistanceInMetersExtractor implements Extractor {
    private static final long serialVersionUID = 3677163735450766164L;

    @Override
    public List<Double> extractDataFrom(List<GPSFixMoving> gpsFixes) {
        List<Double> extractedData = new ArrayList<Double>();
        if (gpsFixes.size() > 1) {
            for (int i = 0; i < gpsFixes.size() - 1; i++) {
                GPSFixMoving fix1 = gpsFixes.get(i);
                GPSFixMoving fix2 = gpsFixes.get(i + 1);
                extractedData.add(getDistanceBetween(fix1, fix2));
            }
        } else {
            extractedData.add(0.0);
        }
        return extractedData;
    }

    private Double getDistanceBetween(GPSFixMoving firstFix, GPSFixMoving secondFix) {
        return firstFix.getPosition().getDistance(secondFix.getPosition()).getMeters();
    }

}
