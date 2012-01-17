package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.confidence.HasConfidence;

/**
 * A position with a confidence attached. The scalable intermediate type represents the x/y/z coordinate of the
 * position on the planet's sphere which can then be scaled and mapped back to the sphere when dividing.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface PositionWithConfidence extends HasConfidence<Triple<Double, Double, Double>, Position> {
    Position getPosition();
}
