package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.tracking.DynamicTrackWithRemove;
import com.sap.sse.common.Timed;

public class DynamicTrackWithRemoveImpl<FixType extends Timed> extends DynamicTrackImpl<FixType> implements DynamicTrackWithRemove<FixType> {
    private static final long serialVersionUID = 2124397433912003485L;

    public DynamicTrackWithRemoveImpl(String nameForReadWriteLock) {
        super(nameForReadWriteLock);
    }

    @Override
    public boolean remove(FixType fix) {
        return getInternalFixes().remove(fix);
    }
    
    @Override
    public void removeAllUpToAndIncluding(FixType fix) {
        getInternalFixes().removeAllLessOrEqual(fix);
    }
}
