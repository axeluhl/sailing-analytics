package com.sap.sailing.datamining.impl;

import com.sap.sailing.domain.base.Moving;

public class SpeedInKnotsExtractor extends AbstractExtractor<Moving, Integer> {

    public SpeedInKnotsExtractor() {
        super("speed in knots");
    }

    @Override
    public Integer extract(Moving dataEntry) {
        return (int) dataEntry.getSpeed().getKnots();
    }

}
