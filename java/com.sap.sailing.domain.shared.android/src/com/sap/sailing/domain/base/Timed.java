package com.sap.sailing.domain.base;

import java.io.Serializable;

import com.sap.sse.common.TimePoint;

public interface Timed extends Serializable {
    TimePoint getTimePoint();
}
