package com.sap.sailing.polars.caching;

import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.polars.data.impl.PolarFixImpl;
import com.sap.sailing.util.SmartFutureCache;

public class PolarFixCache extends
        SmartFutureCache<BoatClass, Map<RegattaAndRaceIdentifier, List<PolarFixImpl>>, PolarFixCacheRaceInterval> {

    public PolarFixCache() {
        super(new PolarFixCacheUpdater(), "polarFixCache");
    }

}
