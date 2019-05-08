package com.sap.sailing.domain.orc.impl;

import com.sap.sailing.domain.orc.ORCPerformanceCurveLeg;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;

public class ORCPerformanceCurveLegImpl implements ORCPerformanceCurveLeg {

    private Distance length;
    private Bearing twa;
    
    public ORCPerformanceCurveLegImpl(Distance length, Bearing twa) {
        this.length = length;
        this.twa = twa;
    }
    
    @Override
    public Distance getLength() {
        return length;
    }

    @Override
    public Bearing getTwa() {
        return twa;
    }

}
