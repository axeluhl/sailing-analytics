package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.tracking.WindSummary;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;

public class WindSummaryImpl implements WindSummary {
    private final Bearing trueWindDirection;
    private final Speed trueLowerboundWind;
    private final Speed trueUpperboundWind;

    public WindSummaryImpl(Bearing trueWindDirection, Speed trueLowerboundWind, Speed trueUpperboundWind) {
        super();
        this.trueWindDirection = trueWindDirection;
        this.trueLowerboundWind = trueLowerboundWind;
        this.trueUpperboundWind = trueUpperboundWind;
    }

    @Override
    public Bearing getTrueWindDirection() {
        return trueWindDirection;
    }

    @Override
    public Speed getTrueLowerboundWind() {
        return trueLowerboundWind;
    }

    @Override
    public Speed getTrueUpperboundWind() {
        return trueUpperboundWind;
    }
}
