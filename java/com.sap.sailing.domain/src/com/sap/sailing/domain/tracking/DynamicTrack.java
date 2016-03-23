package com.sap.sailing.domain.tracking;

import com.sap.sse.common.Timed;

public interface DynamicTrack<FixType extends Timed> extends Track<FixType> {
    /**
     * Adds the fix to this track. An equal fix will be replaced.
     */
    void add(FixType fix);
}
