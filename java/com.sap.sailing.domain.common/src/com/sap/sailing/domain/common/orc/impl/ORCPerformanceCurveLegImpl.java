package com.sap.sailing.domain.common.orc.impl;

import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLeg;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLegTypes;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;

public class ORCPerformanceCurveLegImpl implements ORCPerformanceCurveLeg {
    private Distance length;
    private Bearing twa;
    private ORCPerformanceCurveLegTypes type;
    
    public ORCPerformanceCurveLegImpl(Distance length, Bearing twa) {
        this.length = length;
        this.twa = twa;
        this.type = ORCPerformanceCurveLegTypes.TWA;
    }
    
    public ORCPerformanceCurveLegImpl(Distance length, ORCPerformanceCurveLegTypes type) {
        this.length = length;
        this.type = type;
        this.twa = null;
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
        return "[length=" + length.getNauticalMiles() + "NM, "+(twa==null?type.name():"TWA=" + twa.getDegrees() + "°")+"]";
    }

    @Override
    public ORCPerformanceCurveLegTypes getType() {
        return type;
    }
}
