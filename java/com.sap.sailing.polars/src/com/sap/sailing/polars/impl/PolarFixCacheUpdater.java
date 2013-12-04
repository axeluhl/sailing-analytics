package com.sap.sailing.polars.impl;

import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.polarsheets.PolarFix;
import com.sap.sailing.util.SmartFutureCache.AbstractCacheUpdater;
import com.sap.sailing.util.SmartFutureCache.EmptyUpdateInterval;

public class PolarFixCacheUpdater extends AbstractCacheUpdater<BoatClass, List<PolarFix>, EmptyUpdateInterval> {

    @Override
    public List<PolarFix> computeCacheUpdate(BoatClass key, EmptyUpdateInterval updateInterval) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
