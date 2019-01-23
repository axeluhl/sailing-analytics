package com.sap.sailing.windestimation;

import java.io.IOException;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.data.persistence.polars.PolarDataServiceAccessUtil;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverFeatures;
import com.sap.sailing.windestimation.model.regressor.twdtransition.GaussianBasedTwdTransitionDistributionCache;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStore;
import com.sap.sse.mongodb.MongoDBService;

public class DefaultModelCaches {

    private static final ModelStore MODEL_STORE = new MongoDbModelStore(MongoDBService.INSTANCE.getDB());

    public static final GaussianBasedTwdTransitionDistributionCache GAUSSIAN_TWD_DELTA_TRANSITION_DISTRIBUTION_CACHE = new GaussianBasedTwdTransitionDistributionCache(
            MODEL_STORE, Long.MAX_VALUE);

    public static final ManeuverClassifiersCache MANEUVER_CLASSIFIERS_CACHE;

    static {
        PolarDataService persistedPolarService;
        try {
            persistedPolarService = PolarDataServiceAccessUtil.getPersistedPolarService();
        } catch (ClassNotFoundException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        MANEUVER_CLASSIFIERS_CACHE = new ManeuverClassifiersCache(MODEL_STORE, persistedPolarService, Long.MAX_VALUE,
                new ManeuverFeatures(true, false, false));
    }

    private DefaultModelCaches() {
    }

}
