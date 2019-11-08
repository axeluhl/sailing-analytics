package com.sap.sailing.domain.common.orc;

import java.io.Serializable;

import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;

public interface ORCPerformanceCurveLeg extends Serializable {

    Distance getLength();
    
    /**
     * Only legs of {@link #getType() type} {@link ORCPerformanceCurveLegTypes#TWA} will return
     * a non-{@code null} value here.
     */
    Bearing getTwa();
    
    String toString();
    
    ORCPerformanceCurveLegTypes getType();
    
    /**
     * Scales this leg in length by factor {@code share}. The following relation holds:
     * {@code this.scale(x).getLength().divide(this.getLength()) == x}. Furthermore,
     * {@code Util.equalsWithNull(this.scale(x).getTwa(), this.getTwa())} and
     * {@code this.getType() == this.scale(x).getType()} will both be true.
     */
    ORCPerformanceCurveLeg scale(double share);
}
