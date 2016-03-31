package com.sap.sailing.domain.common.tracking;

import com.sap.sse.common.Timed;

public interface DoubleVectorFix extends Timed {
    double[] get();
    double get(int index);
}
