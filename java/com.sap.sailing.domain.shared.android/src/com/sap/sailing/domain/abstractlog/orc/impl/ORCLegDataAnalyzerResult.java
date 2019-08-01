package com.sap.sailing.domain.abstractlog.orc.impl;

import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLeg;
import com.sap.sailing.domain.common.orc.impl.ORCPerformanceCurveLegImpl;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;

public class ORCLegDataAnalyzerResult {

    private final ORCPerformanceCurveLeg leg;
    private final int legNr;
    
    public ORCLegDataAnalyzerResult(Distance length, Bearing twa, int legNr) {
        this.leg = new ORCPerformanceCurveLegImpl(length, twa);
        this.legNr = legNr;
    }

    public ORCPerformanceCurveLeg getLeg() {
        return leg;
    }

    public int getLegNr() {
        return legNr;
    }
    
}
