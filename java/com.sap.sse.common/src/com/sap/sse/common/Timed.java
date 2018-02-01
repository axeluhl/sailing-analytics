package com.sap.sse.common;

import java.io.Serializable;

public interface Timed extends Serializable {
    TimePoint getTimePoint();
}
