package com.sap.sailing.polars.caching;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.impl.PolarSheetGenerationSettingsImpl;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.generation.PolarSheetGenerator;
import com.sap.sailing.util.SmartFutureCache.CacheUpdater;
import com.sap.sailing.util.SmartFutureCache.EmptyUpdateInterval;

public class PolarSheetPerBoatClassCacheUpdater implements
        CacheUpdater<BoatClass, PolarSheetsData, EmptyUpdateInterval> {

    private final PolarDataService polarDataService;

    public PolarSheetPerBoatClassCacheUpdater(PolarDataService polarDataService) {
        this.polarDataService = polarDataService;
    }

    @Override
    public PolarSheetsData computeCacheUpdate(BoatClass key, EmptyUpdateInterval updateInterval) throws Exception {
        PolarSheetGenerationSettings settings = PolarSheetGenerationSettingsImpl.createStandardPolarSettings();
        PolarSheetGenerator generator = new PolarSheetGenerator(polarDataService.getPolarFixesForBoatClass(key),
                settings);
        PolarSheetsData generationResult = generator.generate();
        return generationResult;
    }

    @Override
    public PolarSheetsData provideNewCacheValue(BoatClass key, PolarSheetsData oldCacheValue,
            PolarSheetsData computedCacheUpdate, EmptyUpdateInterval updateInterval) {
        return computedCacheUpdate;
    }

}
