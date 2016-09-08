package com.sap.sailing.domain.base;

import com.sap.sse.common.NamedWithID;

/**
 * A side line in a race course
 * 
 * @author Frank Mittag (C5163874)
 *
 */
public interface Sideline extends NamedWithID {
    Iterable<Mark> getMarks();
}
