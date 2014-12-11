package com.sap.sailing.polars.caching;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.util.SmartFutureCache;

public class PolarSheetPerBoatClassCache extends
        SmartFutureCache<BoatClass, PolarSheetsData, com.sap.sailing.util.SmartFutureCache.EmptyUpdateInterval>
        implements PolarFixCacheUpdateDoneListener {

    public PolarSheetPerBoatClassCache(PolarDataService polarDataService) {
        super(new PolarSheetPerBoatClassCacheUpdater(polarDataService), "PolarSheetPerBoatClassCache");
    }

    @Override
    public void cacheUpdateDoneForBoatClass(BoatClass key) {
        triggerUpdate(key, new EmptyUpdateInterval());
    }

}
