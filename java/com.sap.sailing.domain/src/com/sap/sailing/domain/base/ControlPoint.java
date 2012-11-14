package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;

public interface ControlPoint extends Named {
    Iterable<SingleMark> getMarks();
}
