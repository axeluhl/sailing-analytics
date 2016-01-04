package com.sap.sailing.domain.tracking;

import com.sap.sse.common.Timed;

public interface DynamicTrack<FixType extends Timed> extends Track<FixType> {
    /**
     * @return <code>true</code> if the element was added, <code>false</code> otherwise.
     */
    boolean add(FixType fix);
}
