package com.sap.sailing.domain.common.tracking;

import com.sap.sse.common.Timed;

/**
 * A fix that simply holds an array of double values. The interpretation of the data depends on the concrete mapping in
 * the RegattaLog.
 */
public interface DoubleVectorFix extends Timed {
    double[] get();
    double get(int index);
}
