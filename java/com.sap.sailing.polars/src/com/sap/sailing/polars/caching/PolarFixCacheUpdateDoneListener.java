package com.sap.sailing.polars.caching;

import com.sap.sailing.domain.base.BoatClass;

public interface PolarFixCacheUpdateDoneListener {

    void cacheUpdateDoneForBoatClass(BoatClass key);

}
