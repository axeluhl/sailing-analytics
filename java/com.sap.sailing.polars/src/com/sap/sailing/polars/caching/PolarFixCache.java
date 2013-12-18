package com.sap.sailing.polars.caching;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.polars.data.PolarFix;
import com.sap.sailing.util.SmartFutureCache;

public class PolarFixCache extends
        SmartFutureCache<BoatClass, Map<RegattaAndRaceIdentifier, List<PolarFix>>, PolarFixCacheRaceInterval> {

    public PolarFixCache(Executor executor) {
        super(new PolarFixCacheUpdater(executor), "polarFixCache");
    }

}
