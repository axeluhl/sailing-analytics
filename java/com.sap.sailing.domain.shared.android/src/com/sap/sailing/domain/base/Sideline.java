package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.WithID;

/**
 * A side line in a race course
 * @author Frank (C5163874)
 *
 */
public interface Sideline extends Named, WithID {
    Iterable<ControlPoint> getControlPoints();

    Iterable<Mark> getMarks();
}
