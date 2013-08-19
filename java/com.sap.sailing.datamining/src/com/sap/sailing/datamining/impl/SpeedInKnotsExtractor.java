package com.sap.sailing.datamining.impl;

import com.sap.sailing.domain.base.Moving;

public class SpeedInKnotsExtractor extends AbstractExtractor<Moving, Double> {

    public SpeedInKnotsExtractor() {
        super("speed in knots");
    }

    @Override
    public Double extract(Moving dataEntry) {
        return dataEntry.getSpeed().getKnots();
    }

}
