package com.sap.sailing.domain.base;

import com.sap.sse.common.Named;
import com.sap.sse.common.WithID;

/**
 * A side line in a race course
 * @author Frank (C5163874)
 *
 */
public interface Sideline extends Named, WithID {
    Iterable<Mark> getMarks();
}
