package com.sap.sailing.domain.common.orc.impl;

import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLeg;
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

    @Override
    public String toString() {
        return "[length=" + length.getNauticalMiles() + "NM, twa=" + twa.getDegrees() + "°]";
    }
}
