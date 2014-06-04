package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.WithID;
import com.sap.sse.common.Named;

public interface ControlPoint extends WithID, Named {
    Iterable<Mark> getMarks();
}
