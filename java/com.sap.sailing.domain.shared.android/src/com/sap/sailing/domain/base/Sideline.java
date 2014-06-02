package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.WithID;
import com.sap.sse.common.Named;

/**
 * A side line in a race course
 * @author Frank (C5163874)
 *
 */
public interface Sideline extends Named, WithID {
    Iterable<Mark> getMarks();
}
