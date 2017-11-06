package com.sap.sailing.domain.common.tracking;

import com.sap.sse.common.Timed;

/**
 * A fix that simply holds an array of {@link Double} values. The interpretation of the data depends on the concrete
 * mapping in the RegattaLog. Asking beyond the end of the vector using {@link #get(int)} will return {@code null} but
 * not throw an exception.
 */
public interface DoubleVectorFix extends Timed {
    Double[] get();
    Double get(int index);
    /**
     * Tells whether at least one component is not {@code null}
     */
    boolean hasValidData();
}
