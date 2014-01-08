package com.sap.sailing.polars.caching;

import java.util.Set;

import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.data.PolarFix;

public class NoCacheEntryException extends Exception {

    private static final long serialVersionUID = 5547683568438235668L;
    private final Set<TrackedRace> notCached;
    private final Set<PolarFix> resultList;

    public NoCacheEntryException(Set<TrackedRace> notCached, Set<PolarFix> resultList) {
        this.notCached = notCached;
        this.resultList = resultList;
    }

    public Set<TrackedRace> getNotCached() {
        return notCached;
    }

    public Set<PolarFix> getCachedResultList() {
        return resultList;
    }

}
