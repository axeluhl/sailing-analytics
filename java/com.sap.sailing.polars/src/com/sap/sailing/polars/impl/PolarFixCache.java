package com.sap.sailing.polars.impl;

import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.polarsheets.PolarFix;
import com.sap.sailing.util.SmartFutureCache;

public class PolarFixCache extends
        SmartFutureCache<BoatClass, List<PolarFix>, com.sap.sailing.util.SmartFutureCache.EmptyUpdateInterval> {

    public PolarFixCache() {
        super(new PolarFixCacheUpdater(), "polarFixCache");
    }

}
