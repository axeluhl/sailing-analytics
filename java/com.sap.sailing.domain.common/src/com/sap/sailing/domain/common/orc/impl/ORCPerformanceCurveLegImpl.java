package com.sap.sailing.domain.common.orc.impl;

import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLeg;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLegTypes;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;

public class ORCPerformanceCurveLegImpl implements ORCPerformanceCurveLeg {
    private static final long serialVersionUID = -1402717786643975976L;
    private final Distance length;
    private final Bearing twa;
    private final ORCPerformanceCurveLegTypes type;
    
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
    public ORCPerformanceCurveLeg scale(double share) {
        final Distance scaledLength = getLength().scale(share);
        return getType() == ORCPerformanceCurveLegTypes.TWA ? new ORCPerformanceCurveLegImpl(scaledLength, getTwa()) :
            new ORCPerformanceCurveLegImpl(scaledLength, getType());
    }

    @Override
    public String toString() {
        return "[length=" + length.getNauticalMiles() + "NM, "+(twa==null?type.name():"TWA=" + twa)+"]";
    }

    @Override
    public ORCPerformanceCurveLegTypes getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((length == null) ? 0 : length.hashCode());
        result = prime * result + ((twa == null) ? 0 : twa.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ORCPerformanceCurveLegImpl other = (ORCPerformanceCurveLegImpl) obj;
        if (length == null) {
            if (other.length != null)
                return false;
        } else if (!length.equals(other.length))
            return false;
        if (twa == null) {
            if (other.twa != null)
                return false;
        } else if (!twa.equals(other.twa))
            return false;
        if (type != other.type)
            return false;
        return true;
    }
}
