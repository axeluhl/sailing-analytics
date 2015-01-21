package com.sap.sailing.domain.base;

import com.sap.sse.common.Named;
import com.sap.sse.common.WithID;

public interface ControlPoint extends WithID, Named {
    Iterable<Mark> getMarks();
}
