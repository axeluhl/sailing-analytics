package com.sap.sailing.datamining.impl.workers;

import com.sap.sailing.domain.base.Moving;
import com.sap.sse.datamining.impl.workers.extractors.AbstractExtractionWorker;

public class SpeedInKnotsExtractionWorker extends AbstractExtractionWorker<Moving, Double> {

    @Override
    public Double extract(Moving dataEntry) {
        return dataEntry.getSpeed().getKnots();
    }

}