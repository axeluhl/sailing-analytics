package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.WithID;

public interface ControlPoint extends WithID, Named {
    Iterable<Mark> getMarks();
}
