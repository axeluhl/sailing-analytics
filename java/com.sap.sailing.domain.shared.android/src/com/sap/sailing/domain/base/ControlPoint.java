package com.sap.sailing.domain.base;

import com.sap.sse.common.NamedWithID;

public interface ControlPoint extends NamedWithID {
    Iterable<Mark> getMarks();
}
